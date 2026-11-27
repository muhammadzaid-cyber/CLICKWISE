package com.example.domain.update

import android.content.Context
import android.os.StatFs
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection

class GitHubReleaseClient(
    private val context: Context,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : UpdateGateway {

    override suspend fun fetchLatestReleaseCheck(
        installedVersionCode: Int,
        installedVersionName: String,
        expectedApplicationId: String
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val releaseJson = fetchSmallUtf8Document(
                urlString = UpdateConfig.LATEST_RELEASE_API_URL,
                maxBytes = UpdateConfig.MAX_METADATA_BYTES,
                sendGitHubApiHeaders = true
            )
            val parsedAssets = UpdateReleaseValidator.parseAndValidateGitHubRelease(releaseJson)
                .getOrElse { err ->
                    return@withContext mapThrowableToCheckError(err)
                }

            val metadataJson = fetchSmallUtf8Document(
                urlString = parsedAssets.updateJsonDownloadUrl,
                maxBytes = UpdateConfig.MAX_METADATA_BYTES,
                sendGitHubApiHeaders = false
            )
            val metadata = UpdateReleaseValidator.parseAndValidateUpdateMetadata(
                rawJson = metadataJson,
                expectedApplicationId = expectedApplicationId,
                expectedApiDigestSha256 = parsedAssets.apkApiDigestSha256
            ).getOrElse { err ->
                return@withContext mapThrowableToCheckError(err)
            }

            UpdateReleaseValidator.evaluateUpdateAvailability(
                installedVersionCode = installedVersionCode,
                installedVersionName = installedVersionName,
                releaseAssets = parsedAssets,
                metadata = metadata,
                checkedAtEpochMs = clock()
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Throwable) {
            mapThrowableToCheckError(e)
        }
    }

    override suspend fun downloadApk(
        update: AvailableUpdateInfo,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): DownloadOutcome = withContext(Dispatchers.IO) {
        if (!UpdateReleaseValidator.isTrustedGitHubUrl(update.apkDownloadUrl)) {
            return@withContext DownloadOutcome.Failure(
                reason = UpdateErrorReason.UNTRUSTED_URL,
                message = "Download URL rejected because it is outside the trusted CLICKWISE GitHub Releases origin.",
                messageUr = "ڈاؤن لوڈ لنک مستند CLICKWISE گٹ ہب ریلیز سے باہر ہونے کی وجہ سے مسترد کر دیا گیا۔"
            )
        }

        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val tempFile = File(updatesDir, "${UpdateConfig.EXPECTED_APK_ASSET_NAME}.part")
        val finalFile = File(updatesDir, UpdateConfig.EXPECTED_APK_ASSET_NAME)
        runCatching { tempFile.delete() }
        runCatching { finalFile.delete() }

        val requiredBytes = (if (update.apkSizeBytes > 0L) update.apkSizeBytes else 25L * 1024L * 1024L) +
            UpdateConfig.STORAGE_BUFFER_BYTES
        val availableBytes = runCatching { StatFs(updatesDir.absolutePath).availableBytes }.getOrDefault(Long.MAX_VALUE)
        if (availableBytes < requiredBytes) {
            return@withContext DownloadOutcome.Failure(
                reason = UpdateErrorReason.INSUFFICIENT_STORAGE,
                message = "Not enough free storage space to safely download the update.",
                messageUr = "اپ ڈیٹ محفوظ طریقے سے ڈاؤن لوڈ کرنے کے لیے آلے میں کافی خالی جگہ موجود نہیں ہے۔"
            )
        }

        var connection: HttpsURLConnection? = null
        try {
            connection = openValidatedConnection(
                initialUrl = update.apkDownloadUrl,
                readTimeoutMs = UpdateConfig.DOWNLOAD_READ_TIMEOUT_MS,
                sendGitHubApiHeaders = false
            )
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext DownloadOutcome.Failure(
                    reason = if (responseCode == 403 || responseCode == 429) {
                        UpdateErrorReason.RATE_LIMITED
                    } else {
                        UpdateErrorReason.DOWNLOAD_INTERRUPTED
                    },
                    message = "GitHub release download returned HTTP $responseCode.",
                    messageUr = "گٹ ہب ریلیز ڈاؤن لوڈ میں سرور خرابی (HTTP $responseCode) پیش آئی۔"
                )
            }

            val contentLength = connection.contentLengthLong.let {
                if (it > 0L) it else update.apkSizeBytes
            }
            if (contentLength > UpdateConfig.MAX_APK_BYTES) {
                return@withContext DownloadOutcome.Failure(
                    reason = UpdateErrorReason.INVALID_APK,
                    message = "APK download size exceeds safety limit.",
                    messageUr = "APK فائل کی جسامت مقررہ حفاظتی حد سے زیادہ ہے۔"
                )
            }

            var downloaded = 0L
            var lastReportedAt = 0L
            val buffer = ByteArray(16 * 1024)

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break
                        downloaded += read
                        if (downloaded > UpdateConfig.MAX_APK_BYTES) {
                            throw UpdateValidationException(
                                UpdateErrorReason.INVALID_APK,
                                "Downloaded APK exceeded maximum allowed size.",
                                "ڈاؤن لوڈ کردہ APK فائل مقررہ حد سے تجاوز کر گئی۔"
                            )
                        }
                        output.write(buffer, 0, read)
                        val now = System.currentTimeMillis()
                        if (now - lastReportedAt >= 80L || (contentLength > 0L && downloaded >= contentLength)) {
                            lastReportedAt = now
                            onProgress(downloaded, contentLength)
                        }
                    }
                    output.fd.sync()
                }
            }

            if (downloaded <= 0L || (contentLength > 0L && downloaded != contentLength)) {
                runCatching { tempFile.delete() }
                return@withContext DownloadOutcome.Failure(
                    reason = UpdateErrorReason.DOWNLOAD_INTERRUPTED,
                    message = "APK download ended unexpectedly before all bytes were received.",
                    messageUr = "مکمل فائل موصول ہونے سے پہلے ڈاؤن لوڈ منقطع ہو گیا۔"
                )
            }

            if (finalFile.exists()) finalFile.delete()
            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            onProgress(downloaded, contentLength.takeIf { it > 0L } ?: downloaded)
            DownloadOutcome.Success(finalFile)
        } catch (ce: CancellationException) {
            runCatching { tempFile.delete() }
            throw ce
        } catch (uve: UpdateValidationException) {
            runCatching { tempFile.delete() }
            DownloadOutcome.Failure(uve.reason, uve.message, uve.messageUr)
        } catch (e: IOException) {
            runCatching { tempFile.delete() }
            DownloadOutcome.Failure(
                reason = UpdateErrorReason.DOWNLOAD_INTERRUPTED,
                message = "Download interrupted due to a network or storage error. Please try again.",
                messageUr = "نیٹ ورک یا اسٹوریج خرابی کی وجہ سے ڈاؤن لوڈ رُک گیا۔ براہِ کرم دوبارہ کوشش کریں۔"
            )
        } catch (e: Exception) {
            runCatching { tempFile.delete() }
            DownloadOutcome.Failure(
                reason = UpdateErrorReason.DOWNLOAD_INTERRUPTED,
                message = e.localizedMessage ?: "Download failed unexpectedly.",
                messageUr = "ڈاؤن لوڈ غیر متوقع طور پر ناکام ہو گیا۔"
            )
        } finally {
            connection?.disconnect()
        }
    }

    private suspend fun fetchSmallUtf8Document(
        urlString: String,
        maxBytes: Long,
        sendGitHubApiHeaders: Boolean
    ): String {
        val connection = openValidatedConnection(
            initialUrl = urlString,
            readTimeoutMs = UpdateConfig.READ_TIMEOUT_MS,
            sendGitHubApiHeaders = sendGitHubApiHeaders
        )
        try {
            val status = connection.responseCode
            when {
                status in 200..299 -> {
                    val buffer = ByteArray(8192)
                    val out = ByteArrayOutputStream()
                    var total = 0L
                    connection.inputStream.use { input ->
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val n = input.read(buffer)
                            if (n == -1) break
                            total += n
                            if (total > maxBytes) {
                                throw UpdateValidationException(
                                    UpdateErrorReason.INVALID_METADATA,
                                    "Remote metadata response exceeded safety size limit.",
                                    "ریلیز میٹاڈیٹا کا حجم مقررہ حد سے زیادہ ہے۔"
                                )
                            }
                            out.write(buffer, 0, n)
                        }
                    }
                    return out.toString(Charsets.UTF_8.name())
                }
                status == 404 -> throw UpdateValidationException(
                    UpdateErrorReason.NO_VALID_RELEASE,
                    "No published stable GitHub release was found yet.",
                    "ابھی تک کوئی مستحکم گٹ ہب ریلیز شائع نہیں ہوئی۔"
                )
                status == 403 || status == 429 -> throw UpdateValidationException(
                    UpdateErrorReason.RATE_LIMITED,
                    "GitHub API rate limit reached temporarily. Please try again later.",
                    "گٹ ہب سروس کی عارضی حد پوری ہو گئی ہے۔ براہِ کرم کچھ دیر بعد دوبارہ کوشش کریں۔"
                )
                else -> throw UpdateValidationException(
                    UpdateErrorReason.GITHUB_UNAVAILABLE,
                    "GitHub Releases service returned HTTP $status.",
                    "گٹ ہب ریلیز سروس سے خرابی (HTTP $status) موصول ہوئی۔"
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openValidatedConnection(
        initialUrl: String,
        readTimeoutMs: Int,
        sendGitHubApiHeaders: Boolean
    ): HttpsURLConnection {
        var currentUrl = initialUrl
        repeat(UpdateConfig.MAX_REDIRECTS + 1) {
            if (!UpdateReleaseValidator.isTrustedGitHubUrl(currentUrl)) {
                throw UpdateValidationException(
                    UpdateErrorReason.UNTRUSTED_URL,
                    "Blocked untrusted or non-HTTPS URL during update check.",
                    "اپ ڈیٹ جانچ کے دوران غیر مصدقہ یا غیر محفوظ لنک روک دیا گیا۔"
                )
            }
            val url = URL(currentUrl)
            val conn = (url.openConnection() as? HttpsURLConnection)
                ?: throw UpdateValidationException(
                    UpdateErrorReason.UNTRUSTED_URL,
                    "Only HTTPS connections are permitted for updates.",
                    "اپ ڈیٹس کے لیے صرف محفوظ HTTPS رابطے کی اجازت ہے۔"
                )
            conn.instanceFollowRedirects = false
            conn.connectTimeout = UpdateConfig.CONNECT_TIMEOUT_MS
            conn.readTimeout = readTimeoutMs
            conn.useCaches = false
            conn.setRequestProperty("User-Agent", "CLICKWISE-Android/${BuildConfig.VERSION_NAME}")
            if (sendGitHubApiHeaders && url.host.equals("api.github.com", ignoreCase = true)) {
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }

            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                code == HttpURLConnection.HTTP_MOVED_TEMP ||
                code == 307 ||
                code == 308
            ) {
                val location = conn.getHeaderField("Location")?.trim().orEmpty()
                conn.disconnect()
                if (location.isEmpty()) {
                    throw UpdateValidationException(
                        UpdateErrorReason.GITHUB_UNAVAILABLE,
                        "GitHub returned an incomplete redirect response.",
                        "گٹ ہب سے نامکمل ری ڈائریکٹ جواب موصول ہوا۔"
                    )
                }
                val resolved = URI(currentUrl).resolve(location).toString()
                currentUrl = resolved
            } else {
                return conn
            }
        }
        throw UpdateValidationException(
            UpdateErrorReason.GITHUB_UNAVAILABLE,
            "Too many redirects while contacting GitHub Releases.",
            "گٹ ہب ریلیز سے رابطے کے دوران بہت زیادہ ری ڈائریکٹس پیش آئے۔"
        )
    }

    private fun mapThrowableToCheckError(err: Throwable): UpdateCheckResult.Error {
        return when (err) {
            is UpdateValidationException -> UpdateCheckResult.Error(
                reason = err.reason,
                message = err.message,
                messageUr = err.messageUr
            )
            is UnknownHostException, is SocketTimeoutException, is IOException -> UpdateCheckResult.Error(
                reason = UpdateErrorReason.GITHUB_UNAVAILABLE,
                message = "Unable to reach GitHub Releases right now. Check your internet connection.",
                messageUr = "اس وقت گٹ ہب ریلیز تک رسائی ممکن نہیں۔ براہِ کرم اپنا انٹرنیٹ کنکشن چیک کریں۔"
            )
            else -> UpdateCheckResult.Error(
                reason = UpdateErrorReason.INVALID_METADATA,
                message = "Received unreadable release metadata from GitHub.",
                messageUr = "گٹ ہب سے موصول ہونے والا ریلیز ڈیٹا پڑھا نہیں جا سکا۔"
            )
        }
    }
}
