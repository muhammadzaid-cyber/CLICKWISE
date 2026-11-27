package com.example.domain.update

import android.app.Activity
import android.content.Intent
import com.example.BuildConfig
import com.example.data.UpdatePreferenceSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UpdateManager(
    private val scope: CoroutineScope,
    private val gateway: UpdateGateway,
    private val connectivityChecker: NetworkConnectivityChecker,
    private val integrityVerifier: ApkIntegrityVerifier,
    private val installerLauncher: ApkInstallerLauncher,
    private val preferencesStore: UpdatePreferencesStore,
    private val installedApplicationId: String = BuildConfig.APPLICATION_ID,
    private val installedVersionName: String = BuildConfig.VERSION_NAME,
    private val installedVersionCode: Int = BuildConfig.VERSION_CODE,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _prefsSnapshot = MutableStateFlow(UpdatePreferenceSnapshot())
    val prefsSnapshot: StateFlow<UpdatePreferenceSnapshot> = _prefsSnapshot.asStateFlow()

    private var checkJob: Job? = null
    private var downloadJob: Job? = null

    init {
        scope.launch {
            runCatching {
                withContext(ioDispatcher) {
                    installerLauncher.cleanupStaleUpdates(installedVersionCode)
                }
            }
        }
        scope.launch {
            preferencesStore.updatePreferencesFlow.collect { snap ->
                _prefsSnapshot.value = snap
            }
        }
    }

    fun runStartupAutoCheckIfDue() {
        if (checkJob?.isActive == true || downloadJob?.isActive == true) return
        checkJob = scope.launch {
            val snap = runCatching { preferencesStore.updatePreferencesFlow.first() }
                .getOrDefault(UpdatePreferenceSnapshot())
            _prefsSnapshot.value = snap

            val elapsed = clock() - snap.lastCheckEpochMs
            if (snap.lastCheckEpochMs > 0L && elapsed in 0L until UpdateConfig.AUTO_CHECK_INTERVAL_MS) {
                return@launch
            }
            if (!connectivityChecker.isOnline()) {
                return@launch
            }

            performCheck(isManual = false)
        }
    }

    fun checkForUpdatesManually() {
        if (checkJob?.isActive == true || downloadJob?.isActive == true) return
        checkJob = scope.launch {
            if (!connectivityChecker.isOnline()) {
                _uiState.value = UpdateUiState.Error(
                    reason = UpdateErrorReason.OFFLINE,
                    message = "No internet connection detected. Connect to a network to check for updates.",
                    messageUr = "انٹرنیٹ کنکشن دستیاب نہیں ہے۔ اپ ڈیٹس چیک کرنے کے لیے نیٹ ورک سے جڑیں۔"
                )
                return@launch
            }
            performCheck(isManual = true)
        }
    }

    private suspend fun performCheck(isManual: Boolean) {
        if (isManual) {
            _uiState.value = UpdateUiState.Checking
        }

        when (val result = gateway.fetchLatestReleaseCheck(
            installedVersionCode = installedVersionCode,
            installedVersionName = installedVersionName,
            expectedApplicationId = installedApplicationId
        )) {
            is UpdateCheckResult.UpToDate -> {
                preferencesStore.recordSuccessfulUpdateCheck(
                    checkedAtEpochMs = result.checkedAtEpochMs,
                    latestVersionName = result.latestVersionName,
                    latestVersionCode = result.latestVersionCode
                )
                _uiState.value = UpdateUiState.UpToDate(
                    latestVersionName = result.latestVersionName,
                    latestVersionCode = result.latestVersionCode
                )
            }

            is UpdateCheckResult.UpdateAvailable -> {
                preferencesStore.recordSuccessfulUpdateCheck(
                    checkedAtEpochMs = result.checkedAtEpochMs,
                    latestVersionName = result.update.metadata.versionName,
                    latestVersionCode = result.update.metadata.versionCode
                )
                val currentSnap = _prefsSnapshot.value
                _uiState.value = UpdateUiState.UpdateAvailable(result.update)

                val shouldSuppressAutoModal = !isManual &&
                    !result.update.isBelowMinimumSupported &&
                    currentSnap.dismissedVersionCode == result.update.metadata.versionCode

                if (!shouldSuppressAutoModal) {
                    _showUpdateDialog.value = true
                }
            }

            is UpdateCheckResult.Error -> {
                if (isManual) {
                    _uiState.value = UpdateUiState.Error(
                        reason = result.reason,
                        message = result.message,
                        messageUr = result.messageUr
                    )
                }
                // On automatic background check failure: remain silent and keep previous check timestamp unchanged.
            }
        }
    }

    fun openUpdateDialogIfAvailable() {
        val state = _uiState.value
        if (state is UpdateUiState.UpdateAvailable ||
            state is UpdateUiState.Downloading ||
            state is UpdateUiState.Verifying ||
            state is UpdateUiState.PermissionRequired ||
            state is UpdateUiState.ReadyToInstall ||
            state is UpdateUiState.Installing ||
            state is UpdateUiState.Error
        ) {
            _showUpdateDialog.value = true
        }
    }

    fun selectLater() {
        val update = extractUpdateFromState(_uiState.value)
        _showUpdateDialog.value = false
        if (update != null) {
            scope.launch {
                preferencesStore.setDismissedUpdateVersionCode(update.metadata.versionCode)
            }
            _uiState.value = UpdateUiState.UpdateAvailable(update)
        }
    }

    fun closeDialogWithoutDismissing() {
        _showUpdateDialog.value = false
    }

    fun startDownloadAndInstall() {
        val update = extractUpdateFromState(_uiState.value) ?: return
        if (downloadJob?.isActive == true) return

        if (!connectivityChecker.isOnline()) {
            _uiState.value = UpdateUiState.Error(
                reason = UpdateErrorReason.OFFLINE,
                message = "No internet connection detected. Unable to download update.",
                messageUr = "انٹرنیٹ کنکشن موجود نہیں ہے۔ اپ ڈیٹ ڈاؤن لوڈ نہیں ہو سکی۔",
                update = update
            )
            _showUpdateDialog.value = true
            return
        }

        _showUpdateDialog.value = true
        downloadJob = scope.launch {
            _uiState.value = UpdateUiState.Downloading(
                update = update,
                bytesDownloaded = 0L,
                totalBytes = update.apkSizeBytes
            )

            val outcome = try {
                gateway.downloadApk(update) { downloaded, total ->
                    _uiState.value = UpdateUiState.Downloading(
                        update = update,
                        bytesDownloaded = downloaded,
                        totalBytes = total
                    )
                }
            } catch (ce: CancellationException) {
                _uiState.value = UpdateUiState.Error(
                    reason = UpdateErrorReason.CANCELLED,
                    message = "Update download was cancelled.",
                    messageUr = "اپ ڈیٹ ڈاؤن لوڈ منسوخ کر دیا گیا۔",
                    update = update
                )
                return@launch
            }

            when (outcome) {
                is DownloadOutcome.Failure -> {
                    _uiState.value = UpdateUiState.Error(
                        reason = outcome.reason,
                        message = outcome.message,
                        messageUr = outcome.messageUr,
                        update = update
                    )
                }

                is DownloadOutcome.Success -> {
                    _uiState.value = UpdateUiState.Verifying(update)
                    val verification = withContext(ioDispatcher) {
                        integrityVerifier.verifyApk(
                            apkFile = outcome.file,
                            expectedSha256 = update.metadata.sha256,
                            expectedApplicationId = installedApplicationId,
                            expectedVersionCode = update.metadata.versionCode,
                            installedVersionCode = installedVersionCode
                        )
                    }

                    when (verification) {
                        is ApkVerificationResult.Invalid -> {
                            withContext(ioDispatcher) { runCatching { outcome.file.delete() } }
                            _uiState.value = UpdateUiState.Error(
                                reason = verification.reason,
                                message = verification.message,
                                messageUr = verification.messageUr,
                                update = update
                            )
                        }

                        ApkVerificationResult.Valid -> {
                            proceedAfterVerification(update, outcome.file.absolutePath)
                        }
                    }
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
    }

    private fun proceedAfterVerification(update: AvailableUpdateInfo, apkPath: String) {
        if (installerLauncher.canRequestPackageInstalls()) {
            _uiState.value = UpdateUiState.ReadyToInstall(update = update, apkPath = apkPath)
        } else {
            _uiState.value = UpdateUiState.PermissionRequired(
                update = update,
                apkPath = apkPath,
                previouslyDenied = false
            )
        }
    }

    fun buildManagePermissionIntent(): Intent = installerLauncher.createManageUnknownSourcesIntent()

    fun onInstallPermissionReturned() {
        val current = _uiState.value
        val update = extractUpdateFromState(current) ?: return
        val apkPath = extractApkPathFromState(current) ?: return

        if (installerLauncher.canRequestPackageInstalls()) {
            _uiState.value = UpdateUiState.ReadyToInstall(update = update, apkPath = apkPath)
        } else {
            _uiState.value = UpdateUiState.PermissionRequired(
                update = update,
                apkPath = apkPath,
                previouslyDenied = true
            )
        }
    }

    fun buildSystemInstallerIntent(): Intent? {
        val current = _uiState.value
        val update = extractUpdateFromState(current) ?: return null
        val apkPath = extractApkPathFromState(current) ?: return null
        val file = File(apkPath)
        if (!file.exists()) {
            _uiState.value = UpdateUiState.Error(
                reason = UpdateErrorReason.INVALID_APK,
                message = "Verified APK file is no longer in cache. Please download again.",
                messageUr = "تصدیق شدہ APK فائل کیش میں موجود نہیں رہی۔ براہِ کرم دوبارہ ڈاؤن لوڈ کریں۔",
                update = update
            )
            return null
        }

        if (!installerLauncher.canRequestPackageInstalls()) {
            _uiState.value = UpdateUiState.PermissionRequired(
                update = update,
                apkPath = apkPath,
                previouslyDenied = false
            )
            return null
        }

        return runCatching {
            val intent = installerLauncher.createInstallApkIntent(file)
            _uiState.value = UpdateUiState.Installing(update = update, apkPath = apkPath)
            intent
        }.getOrElse { err ->
            _uiState.value = UpdateUiState.Error(
                reason = UpdateErrorReason.INSTALL_FAILED,
                message = err.localizedMessage ?: "Unable to launch the Android package installer.",
                messageUr = "اینڈرائیڈ پیکیج انسٹالر شروع نہیں ہو سکا۔",
                update = update,
                apkPath = apkPath
            )
            null
        }
    }

    fun onSystemInstallerResult(resultCode: Int) {
        val current = _uiState.value
        val update = extractUpdateFromState(current) ?: return
        val apkPath = extractApkPathFromState(current)
        val latestInstalledCode = installerLauncher.currentInstalledVersionCode()

        when {
            resultCode == Activity.RESULT_OK || latestInstalledCode >= update.metadata.versionCode -> {
                _uiState.value = UpdateUiState.InstallSucceeded(update.metadata.versionName)
            }
            resultCode == Activity.RESULT_CANCELED -> {
                _uiState.value = UpdateUiState.Error(
                    reason = UpdateErrorReason.INSTALL_CANCELLED,
                    message = "Android installation was cancelled before completion. Your current app and data remain untouched.",
                    messageUr = "مکمل ہونے سے پہلے تنصیب منسوخ کر دی گئی۔ آپ کی موجودہ ایپ اور ڈیٹا محفوظ ہیں۔",
                    update = update,
                    apkPath = apkPath
                )
            }
            else -> {
                _uiState.value = UpdateUiState.Error(
                    reason = UpdateErrorReason.INSTALL_FAILED,
                    message = "Android package installer could not complete the update (code $resultCode).",
                    messageUr = "اینڈرائیڈ پیکیج انسٹالر اپ ڈیٹ مکمل نہیں کر سکا۔",
                    update = update,
                    apkPath = apkPath
                )
            }
        }
    }

    fun retryFromError() {
        val err = _uiState.value as? UpdateUiState.Error
        if (err == null) {
            checkForUpdatesManually()
            return
        }
        val update = err.update
        val apkPath = err.apkPath
        when {
            update != null && apkPath != null && File(apkPath).exists() -> {
                proceedAfterVerification(update, apkPath)
            }
            update != null -> {
                _uiState.value = UpdateUiState.UpdateAvailable(update)
                startDownloadAndInstall()
            }
            else -> {
                checkForUpdatesManually()
            }
        }
    }

    private fun extractUpdateFromState(state: UpdateUiState): AvailableUpdateInfo? = when (state) {
        is UpdateUiState.UpdateAvailable -> state.update
        is UpdateUiState.Downloading -> state.update
        is UpdateUiState.Verifying -> state.update
        is UpdateUiState.PermissionRequired -> state.update
        is UpdateUiState.ReadyToInstall -> state.update
        is UpdateUiState.Installing -> state.update
        is UpdateUiState.Error -> state.update
        else -> null
    }

    private fun extractApkPathFromState(state: UpdateUiState): String? = when (state) {
        is UpdateUiState.PermissionRequired -> state.apkPath
        is UpdateUiState.ReadyToInstall -> state.apkPath
        is UpdateUiState.Installing -> state.apkPath
        is UpdateUiState.Error -> state.apkPath
        else -> null
    }
}
