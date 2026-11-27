package com.example

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.UpdatePreferenceSnapshot
import com.example.domain.model.AppLanguage
import com.example.domain.update.AvailableUpdateInfo
import com.example.domain.update.RemoteUpdateMetadata
import com.example.domain.update.UpdateCheckResult
import com.example.domain.update.UpdateErrorReason
import com.example.domain.update.UpdateReleaseValidator
import com.example.domain.update.UpdateUiState
import com.example.ui.components.AppUpdateStatusPanel
import com.example.ui.components.UpdateAvailableDialog
import com.example.ui.theme.ClickwiseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UpdateValidatorAndUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `manifest declares REQUEST_INSTALL_PACKAGES and ACCESS_NETWORK_STATE`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val perms = info.requestedPermissions?.toSet().orEmpty()
        assertTrue(perms.contains(android.Manifest.permission.INTERNET))
        assertTrue(perms.contains(android.Manifest.permission.ACCESS_NETWORK_STATE))
        assertTrue(perms.contains(android.Manifest.permission.REQUEST_INSTALL_PACKAGES))
    }

    @Test
    fun `release validator rejects drafts, prereleases, mismatched app ids, and digest mismatches`() {
        val draftRelease = """
            {
              "tag_name": "v1.0.1",
              "draft": true,
              "prerelease": false,
              "html_url": "https://github.com/muhammadzaid-cyber/CLICKWISE/releases/tag/v1.0.1",
              "assets": []
            }
        """.trimIndent()
        val draftRes = UpdateReleaseValidator.parseAndValidateGitHubRelease(draftRelease)
        assertTrue(draftRes.isFailure)

        val stableRelease = """
            {
              "tag_name": "v1.0.1",
              "draft": false,
              "prerelease": false,
              "html_url": "https://github.com/muhammadzaid-cyber/CLICKWISE/releases/tag/v1.0.1",
              "body": "Release notes body",
              "assets": [
                {
                  "name": "CLICKWISE.apk",
                  "size": 22000000,
                  "digest": "sha256:${"b".repeat(64)}",
                  "browser_download_url": "https://github.com/muhammadzaid-cyber/CLICKWISE/releases/download/v1.0.1/CLICKWISE.apk"
                },
                {
                  "name": "update.json",
                  "size": 300,
                  "browser_download_url": "https://github.com/muhammadzaid-cyber/CLICKWISE/releases/download/v1.0.1/update.json"
                }
              ]
            }
        """.trimIndent()
        val parsed = UpdateReleaseValidator.parseAndValidateGitHubRelease(stableRelease).getOrThrow()

        val validUpdateJson = """
            {
              "applicationId": "com.aistudio.clickwise.xkqmzt",
              "versionName": "1.0.1",
              "versionCode": 2,
              "minimumSupportedVersionCode": 1,
              "releaseNotes": "Added wireless update validation.",
              "apkAssetName": "CLICKWISE.apk",
              "sha256": "${"b".repeat(64)}"
            }
        """.trimIndent()
        val meta = UpdateReleaseValidator.parseAndValidateUpdateMetadata(
            rawJson = validUpdateJson,
            expectedApplicationId = "com.aistudio.clickwise.xkqmzt",
            expectedApiDigestSha256 = parsed.apkApiDigestSha256
        ).getOrThrow()

        val check = UpdateReleaseValidator.evaluateUpdateAvailability(
            installedVersionCode = 1,
            installedVersionName = "1.0.0",
            releaseAssets = parsed,
            metadata = meta,
            checkedAtEpochMs = 100L
        )
        assertTrue(check is UpdateCheckResult.UpdateAvailable)

        val mismatchedDigestMeta = validUpdateJson.replace("b".repeat(64), "c".repeat(64))
        val badRes = UpdateReleaseValidator.parseAndValidateUpdateMetadata(
            rawJson = mismatchedDigestMeta,
            expectedApplicationId = "com.aistudio.clickwise.xkqmzt",
            expectedApiDigestSha256 = parsed.apkApiDigestSha256
        )
        assertTrue(badRes.isFailure)
    }

    @Test
    fun `update dialog and settings update panel render and handle install or later actions`() {
        val update = AvailableUpdateInfo(
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
                releaseNotes = "Hardened URL telemetry and wireless updater.",
                apkAssetName = "CLICKWISE.apk",
                sha256 = "a".repeat(64)
            ),
            isBelowMinimumSupported = false
        )

        var installClicked = false
        var laterClicked = false
        composeRule.setContent {
            ClickwiseTheme {
                UpdateAvailableDialog(
                    visible = true,
                    state = UpdateUiState.UpdateAvailable(update),
                    installedVersionName = "1.0.0",
                    installedVersionCode = 1,
                    language = AppLanguage.EN,
                    reduceMotion = true,
                    onDownloadAndInstall = { installClicked = true },
                    onSelectLater = { laterClicked = true },
                    onCancelDownload = {},
                    onOpenPermissionSettings = {},
                    onLaunchSystemInstaller = {},
                    onRetry = {},
                    onClose = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("A NEW CLICKWISE VERSION IS AVAILABLE").assertIsDisplayed()
        composeRule.onNodeWithText("Version 1.0.1").assertIsDisplayed()
        composeRule.onNodeWithText("Hardened URL telemetry and wireless updater.").assertIsDisplayed()

        composeRule.onNodeWithTag("update_dialog_install_button").performClick()
        composeRule.onNodeWithTag("update_dialog_later_button").performClick()
        assertTrue(installClicked)
        assertTrue(laterClicked)
    }

    @Test
    fun `settings update panel shows honest failure state and triggers manual check`() {
        var manualCheckTriggered = false
        composeRule.setContent {
            ClickwiseTheme {
                AppUpdateStatusPanel(
                    language = AppLanguage.EN,
                    installedVersionName = "1.0.0",
                    installedVersionCode = 1,
                    updateState = UpdateUiState.Error(
                        reason = UpdateErrorReason.GITHUB_UNAVAILABLE,
                        message = "Unable to reach GitHub Releases right now.",
                        messageUr = "گٹ ہب دستیاب نہیں۔"
                    ),
                    preferences = UpdatePreferenceSnapshot(),
                    onCheckForUpdates = { manualCheckTriggered = true },
                    onOpenUpdateDialog = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("1.0.0 (Build 1)").assertIsDisplayed()
        composeRule.onNodeWithText("Unavailable (check failed)").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_check_updates_button").performClick()
        assertEquals(true, manualCheckTriggered)
    }
}
