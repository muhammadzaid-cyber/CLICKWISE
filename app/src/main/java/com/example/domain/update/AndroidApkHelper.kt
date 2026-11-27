package com.example.domain.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale

class AndroidNetworkConnectivityChecker(
    private val context: Context
) : NetworkConnectivityChecker {
    override fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class AndroidApkIntegrityVerifier(
    private val context: Context
) : ApkIntegrityVerifier {

    override fun verifyApk(
        apkFile: File,
        expectedSha256: String,
        expectedApplicationId: String,
        expectedVersionCode: Int,
        installedVersionCode: Int
    ): ApkVerificationResult {
        if (!apkFile.exists() || !apkFile.isFile || apkFile.length() <= 0L) {
            return ApkVerificationResult.Invalid(
                reason = UpdateErrorReason.INVALID_APK,
                message = "Downloaded APK file is missing or empty.",
                messageUr = "ڈاؤن لوڈ کردہ APK فائل غائب یا خالی ہے۔"
            )
        }

        val actualSha256 = runCatching { computeSha256Hex(apkFile) }.getOrNull()
        if (actualSha256 == null || !actualSha256.equals(expectedSha256.trim(), ignoreCase = true)) {
            return ApkVerificationResult.Invalid(
                reason = UpdateErrorReason.CHECKSUM_MISMATCH,
                message = "SHA-256 verification failed. The downloaded file may be corrupted or altered.",
                messageUr = "SHA-256 تصدیق ناکام ہو گئی۔ ڈاؤن لوڈ کردہ فائل خراب یا تبدیل شدہ ہو سکتی ہے۔"
            )
        }

        val pm = context.packageManager
        val archiveInfo = getArchivePackageInfo(pm, apkFile.absolutePath)
            ?: return ApkVerificationResult.Invalid(
                reason = UpdateErrorReason.INVALID_APK,
                message = "Downloaded file is not a valid Android APK package.",
                messageUr = "ڈاؤن لوڈ کردہ فائل درست اینڈرائیڈ APK پیکیج نہیں ہے۔"
            )

        if (archiveInfo.packageName != expectedApplicationId) {
            return ApkVerificationResult.Invalid(
                reason = UpdateErrorReason.INVALID_APK,
                message = "Package identity mismatch (${archiveInfo.packageName}). Installation blocked.",
                messageUr = "پیکیج شناخت مطابقت نہیں رکھتی۔ سیکیورٹی کے تحت تنصیب روک دی گئی۔"
            )
        }

        val archiveVersionCode = PackageInfoCompat.getLongVersionCode(archiveInfo).toInt()
        if (archiveVersionCode != expectedVersionCode || archiveVersionCode <= installedVersionCode) {
            return ApkVerificationResult.Invalid(
                reason = UpdateErrorReason.INVALID_APK,
                message = "APK version code ($archiveVersionCode) does not represent a valid upgrade.",
                messageUr = "APK ورژن کوڈ ($archiveVersionCode) درست اپ گریڈ کی تصدیق نہیں کرتا۔"
            )
        }

        val installedSigners = extractInstalledSigningDigests(pm, expectedApplicationId)
        val archiveSigners = extractSigningDigests(archiveInfo)
        if (installedSigners.isNotEmpty() &&
            archiveSigners.isNotEmpty() &&
            installedSigners.intersect(archiveSigners).isEmpty()
        ) {
            return ApkVerificationResult.Invalid(
                reason = UpdateErrorReason.SIGNATURE_MISMATCH,
                message = "APK signing certificate does not match the installed CLICKWISE signature.",
                messageUr = "APK کا دستخطی سرٹیفکیٹ موجودہ CLICKWISE ایپ کے سرٹیفکیٹ سے مطابقت نہیں رکھتا۔"
            )
        }

        return ApkVerificationResult.Valid
    }

    private fun computeSha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(16 * 1024)
        FileInputStream(file).use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(Locale.US, it) }
    }

    @Suppress("DEPRECATION")
    private fun getArchivePackageInfo(pm: PackageManager, apkPath: String): PackageInfo? {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            pm.getPackageArchiveInfo(apkPath, flags)
        }
    }

    @Suppress("DEPRECATION")
    private fun extractInstalledSigningDigests(pm: PackageManager, packageName: String): Set<String> {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        val info = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                pm.getPackageInfo(packageName, flags)
            }
        }.getOrNull() ?: return emptySet()
        return extractSigningDigests(info)
    }

    @Suppress("DEPRECATION")
    private fun extractSigningDigests(info: PackageInfo): Set<String> {
        val signatures: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners ?: info.signingInfo?.signingCertificateHistory
        } else {
            info.signatures
        }
        if (signatures == null) return emptySet()
        val md = MessageDigest.getInstance("SHA-256")
        return signatures.map { sig ->
            md.reset()
            md.digest(sig.toByteArray()).joinToString("") { "%02x".format(Locale.US, it) }
        }.toSet()
    }
}

class AndroidApkInstallerLauncher(
    private val context: Context
) : ApkInstallerLauncher {

    override fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    override fun createManageUnknownSourcesIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    override fun createInstallApkIntent(apkFile: File): Intent {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, UpdateConfig.APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
    }

    @Suppress("DEPRECATION")
    override fun currentInstalledVersionCode(): Int {
        return runCatching {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            PackageInfoCompat.getLongVersionCode(info).toInt()
        }.getOrDefault(1)
    }

    override fun cleanupStaleUpdates(installedVersionCode: Int) {
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) return
        updatesDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".part")) {
                runCatching { file.delete() }
            } else if (file.name == UpdateConfig.EXPECTED_APK_ASSET_NAME) {
                val info = runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageArchiveInfo(
                            file.absolutePath,
                            PackageManager.PackageInfoFlags.of(0L)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
                    }
                }.getOrNull()
                val archiveCode = info?.let { PackageInfoCompat.getLongVersionCode(it).toInt() } ?: 0
                if (archiveCode <= installedVersionCode) {
                    runCatching { file.delete() }
                }
            }
        }
    }
}
