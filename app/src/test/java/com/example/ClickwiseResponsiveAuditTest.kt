package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.ui.navigation.ClickwiseApp
import com.example.ui.theme.ClickwiseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ClickwiseResponsiveAuditTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun dismissSplash() {
        composeRule.waitForIdle()
        if (composeRule.onAllNodes(hasTestTag("brand_entrance_splash")).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("brand_entrance_splash").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 3000) {
            composeRule.onAllNodes(hasTestTag("brand_entrance_splash")).fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitForIdle()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w760dp-h1024dp")
    fun `tablet layout renders NavigationRail, Dashboard bento, Academy 2-col grid, and Settings console`() {
        composeRule.setContent {
            ClickwiseTheme {
                ClickwiseApp()
            }
        }
        dismissSplash()

        // Tablet NavigationRail quick destinations are visible
        composeRule.onNodeWithTag("bottom_nav_dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_scanner").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_url_inspector").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_academy").assertIsDisplayed()

        // Navigate to Academy -> check 2-col grid item
        composeRule.onNodeWithTag("bottom_nav_academy").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("academy_lesson_lesson_phishing").assertIsDisplayed()

        // Navigate to Settings via rail icon / drawer
        composeRule.onNodeWithTag("rail_nav_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Language, Accessibility & Session Controls").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1280dp-h800dp")
    fun `desktop layout renders collapsible DesktopSidebar, URL Inspector anatomy, and About architecture`() {
        composeRule.setContent {
            ClickwiseTheme {
                ClickwiseApp()
            }
        }
        dismissSplash()

        // Desktop sidebar items & collapse toggle
        composeRule.onNodeWithTag("drawer_nav_dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_nav_scanner").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_nav_url_inspector").performClick()
        composeRule.waitForIdle()

        // Inspect sample URL on Desktop to verify UrlAnatomyDiagram & split Facts/Risks render
        composeRule.onNodeWithTag("url_sample_0").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("url_inspector_list").performScrollToNode(hasText("OBSERVED URL COMPONENTS (FACTS)"))
        composeRule.onNodeWithText("OBSERVED URL COMPONENTS (FACTS)").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("url_inspector_list").performScrollToNode(hasText("SUSPECTED PATTERN RISKS"))
        composeRule.onNodeWithText("SUSPECTED PATTERN RISKS").performScrollTo().assertIsDisplayed()

        // Navigate to About & Trust via DesktopSidebar
        composeRule.onNodeWithTag("drawer_nav_about").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ZERO-KEY VERIFICATION PIPELINE").assertIsDisplayed()
    }
}
