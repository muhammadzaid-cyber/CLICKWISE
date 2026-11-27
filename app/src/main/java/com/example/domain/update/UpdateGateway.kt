package com.example.domain.update

import android.content.Intent
import java.io.File

sealed interface DownloadOutcome {
    data class Success(val file: File) : DownloadOutcome
    data class Failure(
        val reason: UpdateErrorReason,
        val message: String,
        val messageUr: String
    ) : DownloadOutcome
}

sealed interface ApkVerificationResult {
    data object Valid : ApkVerificationResult
    data class Invalid(
        val reason: UpdateErrorReason,
        val message: String,
        val messageUr: String
    ) : ApkVerificationResult
}

interface UpdateGateway {
    suspend fun fetchLatestReleaseCheck(
        installedVersionCode: Int,
        installedVersionName: String,
        expectedApplicationId: String
    ): UpdateCheckResult

    suspend fun downloadApk(
        update: AvailableUpdateInfo,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): DownloadOutcome
}

interface NetworkConnectivityChecker {
    fun isOnline(): Boolean
}

interface ApkIntegrityVerifier {
    fun verifyApk(
        apkFile: File,
        expectedSha256: String,
        expectedApplicationId: String,
        expectedVersionCode: Int,
        installedVersionCode: Int
    ): ApkVerificationResult
}

interface ApkInstallerLauncher {
    fun canRequestPackageInstalls(): Boolean
    fun createManageUnknownSourcesIntent(): Intent
    fun createInstallApkIntent(apkFile: File): Intent
    fun currentInstalledVersionCode(): Int
    fun cleanupStaleUpdates(installedVersionCode: Int)
}

interface UpdatePreferencesStore {
    val updatePreferencesFlow: kotlinx.coroutines.flow.Flow<com.example.data.UpdatePreferenceSnapshot>
    suspend fun recordSuccessfulUpdateCheck(
        checkedAtEpochMs: Long,
        latestVersionName: String,
        latestVersionCode: Int
    )
    suspend fun setDismissedUpdateVersionCode(versionCode: Int)
}

