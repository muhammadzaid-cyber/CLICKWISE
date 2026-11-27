package com.example

import android.app.Activity
import android.content.Intent
import com.example.data.UpdatePreferenceSnapshot
import com.example.domain.update.ApkInstallerLauncher
import com.example.domain.update.ApkIntegrityVerifier
import com.example.domain.update.ApkVerificationResult
import com.example.domain.update.AvailableUpdateInfo
import com.example.domain.update.DownloadOutcome
import com.example.domain.update.NetworkConnectivityChecker
import com.example.domain.update.RemoteUpdateMetadata
import com.example.domain.update.SemVerComparator
import com.example.domain.update.UpdateCheckResult
import com.example.domain.update.UpdateConfig
import com.example.domain.update.UpdateErrorReason
import com.example.domain.update.UpdateGateway
import com.example.domain.update.UpdateManager
import com.example.domain.update.UpdatePreferencesStore
import com.example.domain.update.UpdateReleaseValidator
import com.example.domain.update.UpdateUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateSystemTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val sampleUpdate = AvailableUpdateInfo(
        releaseTagName = "v1.0.1",
        releaseHtmlUrl = "https://github.com/muhammadzaid-cyber/CLICKWISE/releases/tag/v1.0.1",
        publishedAt = "2026-11-27T10:00:00Z",
        apkDownloadUrl = "https://github.com/muhammadzaid-cyber/CLICKWISE/releases/download/v1.0.1/CLICKWISE.apk",
        apkSizeBytes = 22_000_000L,
        metadata = RemoteUpdateMetadata(
            applicationId = "com.aistudio.clickwise.xkqmzt",
            versionName = "1.0.1",
            versionCode = 2,
            minimumSupportedVersionCode = 1,
            releaseNotes = "- Improved phishing URL checks\n- Added wireless update verification",
            apkAssetName = "CLICKWISE.apk",
            sha256 = "a".repeat(64)
        ),
        isBelowMinimumSupported = false
    )

    private class FakePreferencesStore(
        initial: UpdatePreferenceSnapshot = UpdatePreferenceSnapshot()
    ) : UpdatePreferencesStore {
        private val flow = MutableStateFlow(initial)
        override val updatePreferencesFlow: Flow<UpdatePreferenceSnapshot> = flow
        val current: UpdatePreferenceSnapshot get() = flow.value

        override suspend fun recordSuccessfulUpdateCheck(
            checkedAtEpochMs: Long,
            latestVersionName: String,
            latestVersionCode: Int
        ) {
            flow.value = flow.value.copy(
                lastCheckEpochMs = checkedAtEpochMs,
                lastKnownLatestVersionName = latestVersionName,
                lastKnownLatestVersionCode = latestVersionCode
            )
        }

        override suspend fun setDismissedUpdateVersionCode(versionCode: Int) {
            flow.value = flow.value.copy(dismissedVersionCode = versionCode)
        }
    }

    private class FakeGateway(
        var checkResult: UpdateCheckResult,
        var downloadHandler: suspend ((Long, Long) -> Unit) -> DownloadOutcome = {
            DownloadOutcome.Failure(UpdateErrorReason.DOWNLOAD_INTERRUPTED, "Stub", "Stub")
        }
    ) : UpdateGateway {
        var fetchCount = 0
        override suspend fun fetchLatestReleaseCheck(
            installedVersionCode: Int,
            installedVersionName: String,
            expectedApplicationId: String
        ): UpdateCheckResult {
            fetchCount++
            return checkResult
        }

        override suspend fun downloadApk(
            update: AvailableUpdateInfo,
            onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
        ): DownloadOutcome = downloadHandler(onProgress)
    }

    private class FakeConnectivity(var online: Boolean = true) : NetworkConnectivityChecker {
        override fun isOnline(): Boolean = online
    }

    private class FakeVerifier(
        var result: ApkVerificationResult = ApkVerificationResult.Valid
    ) : ApkIntegrityVerifier {
        override fun verifyApk(
            apkFile: File,
            expectedSha256: String,
            expectedApplicationId: String,
            expectedVersionCode: Int,
            installedVersionCode: Int
        ): ApkVerificationResult = result
    }

    private class FakeInstaller(
        var canInstall: Boolean = true,
        var reportedInstalledCode: Int = 1
    ) : ApkInstallerLauncher {
        var cleanedUpCode: Int? = null
        override fun canRequestPackageInstalls(): Boolean = canInstall
        override fun createManageUnknownSourcesIntent(): Intent = throw UnsupportedOperationException()
        override fun createInstallApkIntent(apkFile: File): Intent = throw UnsupportedOperationException()
        override fun currentInstalledVersionCode(): Int = reportedInstalledCode
        override fun cleanupStaleUpdates(installedVersionCode: Int) {
            cleanedUpCode = installedVersionCode
        }
    }

    private fun createManager(
        scope: TestScope,
        gateway: FakeGateway,
        connectivity: FakeConnectivity = FakeConnectivity(true),
        verifier: FakeVerifier = FakeVerifier(),
        installer: FakeInstaller = FakeInstaller(),
        prefs: FakePreferencesStore = FakePreferencesStore(),
        installedVersionCode: Int = 1,
        installedVersionName: String = "1.0.0",
        nowMs: () -> Long = { 1_700_000_000_000L }
    ): UpdateManager {
        val managerScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(scope.testScheduler))
        return UpdateManager(
            scope = managerScope,
            gateway = gateway,
            connectivityChecker = connectivity,
            integrityVerifier = verifier,
            installerLauncher = installer,
            preferencesStore = prefs,
            installedApplicationId = "com.aistudio.clickwise.xkqmzt",
            installedVersionName = installedVersionName,
            installedVersionCode = installedVersionCode,
            clock = nowMs,
            ioDispatcher = UnconfinedTestDispatcher(scope.testScheduler)
        )
    }

    @Test
    fun `scenario 1 - no new update available reports up to date and does not show dialog`() = runTest {
        val prefs = FakePreferencesStore()
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpToDate("1.0.0", 1, 1000L)
        )
        val manager = createManager(this, gateway, prefs = prefs)

        manager.checkForUpdatesManually()
        advanceUntilIdle()

        assertEquals(UpdateUiState.UpToDate("1.0.0", 1), manager.uiState.value)
        assertFalse(manager.showUpdateDialog.value)
        assertEquals(1000L, prefs.current.lastCheckEpochMs)
    }

    @Test
    fun `scenario 2 - new version available opens dialog and records check`() = runTest {
        val prefs = FakePreferencesStore()
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, 2000L)
        )
        val manager = createManager(this, gateway, prefs = prefs)

        manager.runStartupAutoCheckIfDue()
        advanceUntilIdle()

        assertTrue(manager.uiState.value is UpdateUiState.UpdateAvailable)
        assertTrue(manager.showUpdateDialog.value)
        assertEquals("1.0.1", prefs.current.lastKnownLatestVersionName)
        assertEquals(2, prefs.current.lastKnownLatestVersionCode)
    }

    @Test
    fun `scenario 3 - selecting later closes dialog and suppresses repeat auto prompt for same version`() = runTest {
        var clockMs = 100_000L
        val prefs = FakePreferencesStore()
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, clockMs)
        )
        val manager = createManager(this, gateway, prefs = prefs, nowMs = { clockMs })

        manager.runStartupAutoCheckIfDue()
        advanceUntilIdle()
        assertTrue(manager.showUpdateDialog.value)

        manager.selectLater()
        advanceUntilIdle()
        assertFalse(manager.showUpdateDialog.value)
        assertEquals(2, prefs.current.dismissedVersionCode)

        // Advance past 24 hours: auto-check runs silently without bothering user again for v2
        clockMs += UpdateConfig.AUTO_CHECK_INTERVAL_MS + 5_000L
        gateway.checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, clockMs)
        manager.runStartupAutoCheckIfDue()
        advanceUntilIdle()
        assertEquals(2, gateway.fetchCount)
        assertFalse(manager.showUpdateDialog.value)

        // Manual check still allows opening the update dialog
        manager.checkForUpdatesManually()
        advanceUntilIdle()
        assertTrue(manager.showUpdateDialog.value)
    }

    @Test
    fun `startup auto check respects 24 hour cooldown limit`() = runTest {
        var clockMs = 1_000_000_000L
        val prefs = FakePreferencesStore(
            UpdatePreferenceSnapshot(lastCheckEpochMs = clockMs - 60_000L)
        )
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpToDate("1.0.0", 1, clockMs)
        )
        val manager = createManager(this, gateway, prefs = prefs, nowMs = { clockMs })

        manager.runStartupAutoCheckIfDue()
        advanceUntilIdle()

        assertEquals(0, gateway.fetchCount)
    }

    @Test
    fun `scenarios 4 and 5 - download completes, verifies checksum and transitions to ready to install`() = runTest {
        val apkFile = tempFolder.newFile("CLICKWISE.apk").apply { writeText("valid-apk-stub") }
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, 5000L),
            downloadHandler = { onProgress ->
                onProgress(11_000_000L, 22_000_000L)
                onProgress(22_000_000L, 22_000_000L)
                DownloadOutcome.Success(apkFile)
            }
        )
        val verifier = FakeVerifier(ApkVerificationResult.Valid)
        val installer = FakeInstaller(canInstall = true)
        val manager = createManager(this, gateway, verifier = verifier, installer = installer)

        manager.checkForUpdatesManually()
        advanceUntilIdle()

        manager.startDownloadAndInstall()
        advanceUntilIdle()

        val state = manager.uiState.value
        assertTrue(state is UpdateUiState.ReadyToInstall)
        assertEquals(apkFile.absolutePath, (state as UpdateUiState.ReadyToInstall).apkPath)
    }

    @Test
    fun `scenario 6 - download interrupted exposes error and retry recovers cleanly`() = runTest {
        val apkFile = tempFolder.newFile("CLICKWISE-retry.apk").apply { writeText("recovered") }
        var attempt = 0
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, 5000L),
            downloadHandler = {
                attempt++
                if (attempt == 1) {
                    DownloadOutcome.Failure(
                        UpdateErrorReason.DOWNLOAD_INTERRUPTED,
                        "Connection lost mid-download.",
                        "ڈاؤن لوڈ کے دوران کنکشن منقطع ہو گیا۔"
                    )
                } else {
                    DownloadOutcome.Success(apkFile)
                }
            }
        )
        val manager = createManager(this, gateway)
        manager.checkForUpdatesManually()
        advanceUntilIdle()

        manager.startDownloadAndInstall()
        advanceUntilIdle()
        val firstState = manager.uiState.value as UpdateUiState.Error
        assertEquals(UpdateErrorReason.DOWNLOAD_INTERRUPTED, firstState.reason)

        manager.retryFromError()
        advanceUntilIdle()
        assertTrue(manager.uiState.value is UpdateUiState.ReadyToInstall)
    }

    @Test
    fun `download cancellation aborts cleanly without erroring out app`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, 5000L),
            downloadHandler = {
                gate.await()
                DownloadOutcome.Failure(UpdateErrorReason.CANCELLED, "Cancelled", "منسوخ")
            }
        )
        val manager = createManager(this, gateway)
        manager.checkForUpdatesManually()
        advanceUntilIdle()

        manager.startDownloadAndInstall()
        advanceUntilIdle()
        assertTrue(manager.uiState.value is UpdateUiState.Downloading)

        manager.cancelDownload()
        advanceUntilIdle()
        val err = manager.uiState.value as UpdateUiState.Error
        assertEquals(UpdateErrorReason.CANCELLED, err.reason)
    }

    @Test
    fun `scenario 7 - invalid checksum deletes downloaded file and blocks install`() = runTest {
        val apkFile = tempFolder.newFile("corrupt.apk").apply { writeText("tampered-bytes") }
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, 5000L),
            downloadHandler = { DownloadOutcome.Success(apkFile) }
        )
        val verifier = FakeVerifier(
            ApkVerificationResult.Invalid(
                UpdateErrorReason.CHECKSUM_MISMATCH,
                "SHA-256 verification failed.",
                "SHA-256 تصدیق ناکام ہو گئی۔"
            )
        )
        val manager = createManager(this, gateway, verifier = verifier)
        manager.checkForUpdatesManually()
        advanceUntilIdle()

        manager.startDownloadAndInstall()
        advanceUntilIdle()

        val state = manager.uiState.value as UpdateUiState.Error
        assertEquals(UpdateErrorReason.CHECKSUM_MISMATCH, state.reason)
        assertFalse("Corrupted APK must be removed after checksum mismatch", apkFile.exists())
    }

    @Test
    fun `scenario 8 - github api unavailable never falsely claims up to date`() = runTest {
        val prefs = FakePreferencesStore(UpdatePreferenceSnapshot(lastCheckEpochMs = 0L))
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.Error(
                UpdateErrorReason.GITHUB_UNAVAILABLE,
                "GitHub Releases unavailable",
                "گٹ ہب دستیاب نہیں"
            )
        )
        val manager = createManager(this, gateway, prefs = prefs)

        manager.checkForUpdatesManually()
        advanceUntilIdle()

        val state = manager.uiState.value
        assertTrue(state is UpdateUiState.Error)
        assertFalse(state is UpdateUiState.UpToDate)
        assertEquals(0L, prefs.current.lastCheckEpochMs)
    }

    @Test
    fun `scenario 9 - denied install permission keeps previouslyDenied state until granted`() = runTest {
        val apkFile = tempFolder.newFile("verified.apk").apply { writeText("ok") }
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, 5000L),
            downloadHandler = { DownloadOutcome.Success(apkFile) }
        )
        val installer = FakeInstaller(canInstall = false)
        val manager = createManager(this, gateway, installer = installer)

        manager.checkForUpdatesManually()
        advanceUntilIdle()
        manager.startDownloadAndInstall()
        advanceUntilIdle()

        val permState1 = manager.uiState.value as UpdateUiState.PermissionRequired
        assertFalse(permState1.previouslyDenied)

        // User returns without granting permission
        manager.onInstallPermissionReturned()
        val permState2 = manager.uiState.value as UpdateUiState.PermissionRequired
        assertTrue(permState2.previouslyDenied)

        // User grants permission and returns
        installer.canInstall = true
        manager.onInstallPermissionReturned()
        assertTrue(manager.uiState.value is UpdateUiState.ReadyToInstall)
    }

    @Test
    fun `scenarios 10 and 11 - installer cancel reports cancellation while verified ok reports success`() = runTest {
        val apkFile = tempFolder.newFile("installable.apk").apply { writeText("ok") }
        val gateway = FakeGateway(
            checkResult = UpdateCheckResult.UpdateAvailable(sampleUpdate, 5000L),
            downloadHandler = { DownloadOutcome.Success(apkFile) }
        )
        val installer = FakeInstaller(canInstall = true, reportedInstalledCode = 1)
        val manager = createManager(this, gateway, installer = installer)

        manager.checkForUpdatesManually()
        advanceUntilIdle()
        manager.startDownloadAndInstall()
        advanceUntilIdle()
        assertTrue(manager.uiState.value is UpdateUiState.ReadyToInstall)

        // Scenario 10: User cancels system installer (versionCode still 1)
        manager.onSystemInstallerResult(Activity.RESULT_CANCELED)
        val cancelledState = manager.uiState.value as UpdateUiState.Error
        assertEquals(UpdateErrorReason.INSTALL_CANCELLED, cancelledState.reason)

        // Non-OK failure code must NOT report success
        manager.onSystemInstallerResult(Activity.RESULT_FIRST_USER)
        val failedState = manager.uiState.value as UpdateUiState.Error
        assertEquals(UpdateErrorReason.INSTALL_FAILED, failedState.reason)

        // Scenario 11: Successful in-place installation
        installer.reportedInstalledCode = 2
        manager.onSystemInstallerResult(Activity.RESULT_OK)
        assertEquals(UpdateUiState.InstallSucceeded("1.0.1"), manager.uiState.value)
    }

    @Test
    fun `scenario 12 - stale update cleanup runs without altering preferences snapshot`() = runTest {
        val prefs = FakePreferencesStore(
            UpdatePreferenceSnapshot(
                lastCheckEpochMs = 42_000L,
                dismissedVersionCode = 1,
                lastKnownLatestVersionName = "1.0.1",
                lastKnownLatestVersionCode = 2
            )
        )
        val installer = FakeInstaller(reportedInstalledCode = 2)
        val gateway = FakeGateway(UpdateCheckResult.UpToDate("1.0.1", 2, 50_000L))

        val manager = createManager(
            this,
            gateway,
            installer = installer,
            prefs = prefs,
            installedVersionCode = 2,
            installedVersionName = "1.0.1"
        )
        advanceUntilIdle()

        assertEquals(2, installer.cleanedUpCode)
        assertEquals(42_000L, prefs.current.lastCheckEpochMs)
        assertEquals("1.0.1", prefs.current.lastKnownLatestVersionName)
    }

    @Test
    fun `semver comparator and trusted github url validation enforce security policy`() {
        assertTrue(SemVerComparator.compare("1.0.1", "1.0.0") > 0)
        assertTrue(SemVerComparator.compare("1.1.0", "1.0.9") > 0)
        assertTrue(SemVerComparator.compare("1.10.0", "1.2.0") > 0)
        assertEquals(0, SemVerComparator.compare("v1.0.1", "1.0.1"))

        assertTrue(
            UpdateReleaseValidator.isTrustedGitHubUrl(
                "https://github.com/muhammadzaid-cyber/CLICKWISE/releases/download/v1.0.1/CLICKWISE.apk"
            )
        )
        assertTrue(
            UpdateReleaseValidator.isTrustedGitHubUrl(
                "https://objects.githubusercontent.com/github-production-release-asset-2e65be/12345/CLICKWISE.apk"
            )
        )
        // Reject HTTP, untrusted hosts, and other repositories
        assertFalse(
            UpdateReleaseValidator.isTrustedGitHubUrl(
                "http://github.com/muhammadzaid-cyber/CLICKWISE/releases/download/v1.0.1/CLICKWISE.apk"
            )
        )
        assertFalse(
            UpdateReleaseValidator.isTrustedGitHubUrl(
                "https://github.com/attacker/CLICKWISE/releases/download/v1.0.1/CLICKWISE.apk"
            )
        )
        assertFalse(
            UpdateReleaseValidator.isTrustedGitHubUrl(
                "https://evil.example.com/CLICKWISE.apk"
            )
        )
    }
}
