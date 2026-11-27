package com.example.domain.update

import org.json.JSONObject
import java.net.URI
import java.util.Locale

object UpdateConfig {
    const val GITHUB_OWNER = "muhammadzaid-cyber"
    const val GITHUB_REPO = "CLICKWISE"
    const val LATEST_RELEASE_API_URL =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    const val EXPECTED_APK_ASSET_NAME = "CLICKWISE.apk"
    const val EXPECTED_METADATA_ASSET_NAME = "update.json"
    const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    const val AUTO_CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L
    const val CONNECT_TIMEOUT_MS = 15_000
    const val READ_TIMEOUT_MS = 20_000
    const val DOWNLOAD_READ_TIMEOUT_MS = 30_000
    const val MAX_METADATA_BYTES = 256 * 1024L
    const val MAX_APK_BYTES = 250L * 1024L * 1024L
    const val STORAGE_BUFFER_BYTES = 10L * 1024L * 1024L
    const val MAX_REDIRECTS = 5

    val TRUSTED_HOSTS = setOf(
        "api.github.com",
        "github.com",
        "objects.githubusercontent.com",
        "release-assets.githubusercontent.com",
        "github-releases.githubusercontent.com"
    )
}

data class RemoteUpdateMetadata(
    val applicationId: String,
    val versionName: String,
    val versionCode: Int,
    val minimumSupportedVersionCode: Int,
    val releaseNotes: String,
    val apkAssetName: String,
    val sha256: String
)

data class AvailableUpdateInfo(
    val releaseTagName: String,
    val releaseHtmlUrl: String,
    val publishedAt: String,
    val apkDownloadUrl: String,
    val apkSizeBytes: Long,
    val metadata: RemoteUpdateMetadata,
    val isBelowMinimumSupported: Boolean
)

enum class UpdateErrorReason {
    OFFLINE,
    GITHUB_UNAVAILABLE,
    RATE_LIMITED,
    NO_VALID_RELEASE,
    INVALID_METADATA,
    UNTRUSTED_URL,
    DOWNLOAD_INTERRUPTED,
    INSUFFICIENT_STORAGE,
    CHECKSUM_MISMATCH,
    INVALID_APK,
    SIGNATURE_MISMATCH,
    INSTALL_PERMISSION_DENIED,
    INSTALL_CANCELLED,
    INSTALL_FAILED,
    CANCELLED
}

sealed interface UpdateCheckResult {
    data class UpToDate(
        val latestVersionName: String,
        val latestVersionCode: Int,
        val checkedAtEpochMs: Long
    ) : UpdateCheckResult

    data class UpdateAvailable(
        val update: AvailableUpdateInfo,
        val checkedAtEpochMs: Long
    ) : UpdateCheckResult

    data class Error(
        val reason: UpdateErrorReason,
        val message: String,
        val messageUr: String
    ) : UpdateCheckResult
}

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class UpToDate(
        val latestVersionName: String,
        val latestVersionCode: Int
    ) : UpdateUiState

    data class UpdateAvailable(
        val update: AvailableUpdateInfo
    ) : UpdateUiState

    data class Downloading(
        val update: AvailableUpdateInfo,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateUiState {
        val progressFraction: Float
            get() = if (totalBytes > 0L) {
                (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
    }

    data class Verifying(
        val update: AvailableUpdateInfo
    ) : UpdateUiState

    data class PermissionRequired(
        val update: AvailableUpdateInfo,
        val apkPath: String,
        val previouslyDenied: Boolean = false
    ) : UpdateUiState

    data class ReadyToInstall(
        val update: AvailableUpdateInfo,
        val apkPath: String
    ) : UpdateUiState

    data class Installing(
        val update: AvailableUpdateInfo,
        val apkPath: String
    ) : UpdateUiState

    data class InstallSucceeded(
        val versionName: String
    ) : UpdateUiState

    data class Error(
        val reason: UpdateErrorReason,
        val message: String,
        val messageUr: String,
        val update: AvailableUpdateInfo? = null,
        val apkPath: String? = null
    ) : UpdateUiState
}

object SemVerComparator {
    fun normalize(raw: String): String =
        raw.trim().removePrefix("v").removePrefix("V")

    fun compare(a: String, b: String): Int {
        val leftParts = parseNumericParts(a)
        val rightParts = parseNumericParts(b)
        val maxLen = maxOf(leftParts.size, rightParts.size)
        for (i in 0 until maxLen) {
            val l = leftParts.getOrElse(i) { 0 }
            val r = rightParts.getOrElse(i) { 0 }
            if (l != r) return l.compareTo(r)
        }
        return normalize(a).compareTo(normalize(b), ignoreCase = true)
    }

    private fun parseNumericParts(version: String): List<Int> {
        val core = normalize(version).substringBefore('-').substringBefore('+')
        return core.split('.').map { it.toIntOrNull() ?: 0 }
    }
}

object UpdateReleaseValidator {
    private val sha256Regex = Regex("^[a-fA-F0-9]{64}$")

    fun isTrustedGitHubUrl(rawUrl: String): Boolean {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return false
        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return false
        if (!uri.scheme.equals("https", ignoreCase = true)) return false
        val host = uri.host?.lowercase(Locale.US) ?: return false
        if (host !in UpdateConfig.TRUSTED_HOSTS) return false
        val path = uri.path.orEmpty()
        return when (host) {
            "github.com" -> path.startsWith(
                "/${UpdateConfig.GITHUB_OWNER}/${UpdateConfig.GITHUB_REPO}/releases/download/"
            )
            "api.github.com" -> path.startsWith(
                "/repos/${UpdateConfig.GITHUB_OWNER}/${UpdateConfig.GITHUB_REPO}/releases/"
            )
            else -> true
        }
    }

    data class ParsedGitHubReleaseAssets(
        val tagName: String,
        val htmlUrl: String,
        val publishedAt: String,
        val releaseBody: String,
        val apkDownloadUrl: String,
        val apkSizeBytes: Long,
        val apkApiDigestSha256: String?,
        val updateJsonDownloadUrl: String,
        val updateJsonSizeBytes: Long
    )

    fun parseAndValidateGitHubRelease(rawJson: String): Result<ParsedGitHubReleaseAssets> {
        return runCatching {
            val root = JSONObject(rawJson)
            if (root.optBoolean("draft", true)) {
                throw UpdateValidationException(
                    UpdateErrorReason.NO_VALID_RELEASE,
                    "Release is marked as draft.",
                    "ریلیز ابھی ڈرافٹ حالت میں ہے۔"
                )
            }
            if (root.optBoolean("prerelease", true)) {
                throw UpdateValidationException(
                    UpdateErrorReason.NO_VALID_RELEASE,
                    "Prerelease versions are ignored for stable updates.",
                    "مستحکم اپ ڈیٹس کے لیے پری ریلیز ورژن نظر انداز کیے جاتے ہیں۔"
                )
            }

            val tagName = root.optString("tag_name").trim()
            val htmlUrl = root.optString("html_url").trim()
            val publishedAt = root.optString("published_at").trim()
            val releaseBody = root.optString("body").trim()

            if (tagName.isEmpty()) {
                throw UpdateValidationException(
                    UpdateErrorReason.INVALID_METADATA,
                    "GitHub release is missing tag_name.",
                    "گٹ ہب ریلیز میں ورژن ٹیگ موجود نہیں ہے۔"
                )
            }

            if (htmlUrl.isNotEmpty() && !htmlUrl.startsWith(
                    "https://github.com/${UpdateConfig.GITHUB_OWNER}/${UpdateConfig.GITHUB_REPO}/releases/"
                )
            ) {
                throw UpdateValidationException(
                    UpdateErrorReason.UNTRUSTED_URL,
                    "Release page URL does not match the trusted CLICKWISE repository.",
                    "ریلیز کا لنک مستند CLICKWISE ذخیرے سے مطابقت نہیں رکھتا۔"
                )
            }

            val assets = root.optJSONArray("assets")
                ?: throw UpdateValidationException(
                    UpdateErrorReason.NO_VALID_RELEASE,
                    "No release assets found in the latest GitHub release.",
                    "تازہ ترین گٹ ہب ریلیز میں کوئی فائل موجود نہیں ہے۔"
                )

            var apkUrl: String? = null
            var apkSize = -1L
            var apkDigest: String? = null
            var metaUrl: String? = null
            var metaSize = -1L

            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name").trim()
                val downloadUrl = asset.optString("browser_download_url").trim()
                val size = asset.optLong("size", -1L)
                when (name) {
                    UpdateConfig.EXPECTED_APK_ASSET_NAME -> {
                        apkUrl = downloadUrl
                        apkSize = size
                        val rawDigest = asset.optString("digest").trim()
                        if (rawDigest.startsWith("sha256:", ignoreCase = true)) {
                            apkDigest = rawDigest.substringAfter(':').trim().lowercase(Locale.US)
                        }
                    }
                    UpdateConfig.EXPECTED_METADATA_ASSET_NAME -> {
                        metaUrl = downloadUrl
                        metaSize = size
                    }
                }
            }

            if (apkUrl.isNullOrEmpty() || metaUrl.isNullOrEmpty()) {
                throw UpdateValidationException(
                    UpdateErrorReason.NO_VALID_RELEASE,
                    "Latest GitHub release is missing CLICKWISE.apk or update.json.",
                    "تازہ ترین ریلیز میں CLICKWISE.apk یا update.json موجود نہیں ہے۔"
                )
            }

            if (!isTrustedGitHubUrl(apkUrl) || !isTrustedGitHubUrl(metaUrl)) {
                throw UpdateValidationException(
                    UpdateErrorReason.UNTRUSTED_URL,
                    "Release asset URL failed HTTPS repository verification.",
                    "ریلیز فائل کا لنک محفوظ HTTPS تصدیق میں ناکام رہا۔"
                )
            }

            if (metaSize > UpdateConfig.MAX_METADATA_BYTES) {
                throw UpdateValidationException(
                    UpdateErrorReason.INVALID_METADATA,
                    "Remote update.json exceeds allowed metadata size.",
                    "اپ ڈیٹ میٹاڈیٹا فائل کی جسامت حد سے زیادہ ہے۔"
                )
            }

            if (apkSize > UpdateConfig.MAX_APK_BYTES) {
                throw UpdateValidationException(
                    UpdateErrorReason.INVALID_APK,
                    "Remote APK exceeds maximum allowed size.",
                    "اپ ڈیٹ APK فائل کی جسامت حد سے زیادہ ہے۔"
                )
            }

            ParsedGitHubReleaseAssets(
                tagName = tagName,
                htmlUrl = htmlUrl,
                publishedAt = publishedAt,
                releaseBody = releaseBody,
                apkDownloadUrl = apkUrl,
                apkSizeBytes = apkSize,
                apkApiDigestSha256 = apkDigest,
                updateJsonDownloadUrl = metaUrl,
                updateJsonSizeBytes = metaSize
            )
        }
    }

    fun parseAndValidateUpdateMetadata(
        rawJson: String,
        expectedApplicationId: String,
        expectedApiDigestSha256: String?
    ): Result<RemoteUpdateMetadata> {
        return runCatching {
            val obj = JSONObject(rawJson)
            val appId = obj.optString("applicationId").trim()
            val versionName = obj.optString("versionName").trim()
            val versionCode = obj.optInt("versionCode", -1)
            val minSupported = obj.optInt("minimumSupportedVersionCode", 1).coerceAtLeast(1)
            val releaseNotes = obj.optString("releaseNotes").trim()
            val apkAssetName = obj.optString("apkAssetName").trim()
            val sha256 = obj.optString("sha256").trim().lowercase(Locale.US)

            if (appId != expectedApplicationId) {
                throw UpdateValidationException(
                    UpdateErrorReason.INVALID_METADATA,
                    "Update metadata applicationId does not match CLICKWISE.",
                    "اپ ڈیٹ میٹاڈیٹا کا پیکیج شناخت کنندہ CLICKWISE سے مطابقت نہیں رکھتا۔"
                )
            }
            if (apkAssetName != UpdateConfig.EXPECTED_APK_ASSET_NAME) {
                throw UpdateValidationException(
                    UpdateErrorReason.INVALID_METADATA,
                    "Unexpected APK asset name in update metadata.",
                    "اپ ڈیٹ میٹاڈیٹا میں غیر متوقع APK فائل کا نام ہے۔"
                )
            }
            if (versionCode <= 0 || versionName.isEmpty()) {
                throw UpdateValidationException(
                    UpdateErrorReason.INVALID_METADATA,
                    "Update metadata contains an invalid versionCode or versionName.",
                    "اپ ڈیٹ میٹاڈیٹا میں ورژن نمبر درست نہیں ہے۔"
                )
            }
            if (!sha256Regex.matches(sha256)) {
                throw UpdateValidationException(
                    UpdateErrorReason.INVALID_METADATA,
                    "Update metadata is missing a valid SHA-256 digest.",
                    "اپ ڈیٹ میٹاڈیٹا میں درست SHA-256 چیک سم موجود نہیں ہے۔"
                )
            }
            if (!expectedApiDigestSha256.isNullOrBlank() &&
                !sha256.equals(expectedApiDigestSha256, ignoreCase = true)
            ) {
                throw UpdateValidationException(
                    UpdateErrorReason.CHECKSUM_MISMATCH,
                    "GitHub release digest does not match update.json SHA-256.",
                    "گٹ ہب ریلیز کا چیک سم update.json سے مطابقت نہیں رکھتا۔"
                )
            }

            RemoteUpdateMetadata(
                applicationId = appId,
                versionName = versionName,
                versionCode = versionCode,
                minimumSupportedVersionCode = minSupported,
                releaseNotes = releaseNotes,
                apkAssetName = apkAssetName,
                sha256 = sha256
            )
        }
    }

    fun evaluateUpdateAvailability(
        installedVersionCode: Int,
        installedVersionName: String,
        releaseAssets: ParsedGitHubReleaseAssets,
        metadata: RemoteUpdateMetadata,
        checkedAtEpochMs: Long
    ): UpdateCheckResult {
        val isNewerByCode = metadata.versionCode > installedVersionCode
        if (!isNewerByCode) {
            return UpdateCheckResult.UpToDate(
                latestVersionName = metadata.versionName,
                latestVersionCode = metadata.versionCode,
                checkedAtEpochMs = checkedAtEpochMs
            )
        }

        val resolvedNotes = metadata.releaseNotes.ifBlank {
            releaseAssets.releaseBody.ifBlank {
                "Stability, security, and threat-inspection improvements."
            }
        }
        val finalMetadata = metadata.copy(releaseNotes = resolvedNotes)
        val isBelowMin = installedVersionCode < metadata.minimumSupportedVersionCode

        return UpdateCheckResult.UpdateAvailable(
            update = AvailableUpdateInfo(
                releaseTagName = releaseAssets.tagName,
                releaseHtmlUrl = releaseAssets.htmlUrl,
                publishedAt = releaseAssets.publishedAt,
                apkDownloadUrl = releaseAssets.apkDownloadUrl,
                apkSizeBytes = releaseAssets.apkSizeBytes,
                metadata = finalMetadata,
                isBelowMinimumSupported = isBelowMin
            ),
            checkedAtEpochMs = checkedAtEpochMs
        )
    }
}

class UpdateValidationException(
    val reason: UpdateErrorReason,
    override val message: String,
    val messageUr: String
) : IllegalStateException(message)
