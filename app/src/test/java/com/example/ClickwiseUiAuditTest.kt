package com.example

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.EducationalContentRepository
import com.example.domain.analyzer.PlaceholderAiRepository
import com.example.domain.analyzer.UrlInspectorEngine
import com.example.domain.model.ActionPriority
import com.example.domain.model.AnalysisFinding
import com.example.domain.model.AnalysisType
import com.example.domain.model.AppLanguage
import com.example.domain.model.FindingSeverity
import com.example.domain.model.MessageAnalysisResult
import com.example.domain.model.RecommendedAction
import com.example.domain.model.RiskLevel
import com.example.ui.navigation.ClickwiseApp
import com.example.ui.screens.AnalysisResultView
import com.example.ui.theme.ClickwiseTheme
import com.example.ui.viewmodel.ClickwiseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ClickwiseUiAuditTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun dismissSplashIfPresent() {
        composeRule.waitForIdle()
        if (composeRule.onAllNodes(hasTestTag("brand_entrance_splash")).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("brand_entrance_splash").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 3000) {
            composeRule.onAllNodes(hasTestTag("brand_entrance_splash")).fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun scrollToTag(listTag: String, targetTag: String) {
        composeRule.onNodeWithTag(listTag).performScrollToNode(hasTestTag(targetTag))
        composeRule.onNodeWithTag(targetTag).performScrollTo()
    }

    private fun scrollToText(listTag: String, text: String) {
        composeRule.onNodeWithTag(listTag).performScrollToNode(hasText(text))
        composeRule.onNodeWithText(text).performScrollTo()
    }

    @Test
    fun `audit all 8 navigation items, scanner validation, url inspector, academy, quiz, history, and language switcher`() {
        composeRule.setContent {
            ClickwiseTheme {
                ClickwiseApp()
            }
        }
        dismissSplashIfPresent()

        // 1. Dashboard hero + navigation to Message Scanner
        scrollToTag("dashboard_list", "dashboard_start_analysis_button")
        composeRule.onNodeWithTag("dashboard_start_analysis_button").performClick()
        composeRule.waitForIdle()

        // 2. Empty scanner validation check
        scrollToTag("scanner_list", "scanner_analyse_button")
        composeRule.onNodeWithTag("scanner_analyse_button").performClick()
        composeRule.waitForIdle()
        scrollToText("scanner_list", "Please enter a message or suspicious URL before running analysis.")
        composeRule.onNodeWithText("Please enter a message or suspicious URL before running analysis.")
            .assertIsDisplayed()

        // 3. Load sample scenario and clear it
        scrollToTag("scanner_list", "scanner_sample_sample_scholarship")
        composeRule.onNodeWithTag("scanner_sample_sample_scholarship").performClick()
        composeRule.waitForIdle()
        scrollToTag("scanner_list", "scanner_clear_button")
        composeRule.onNodeWithTag("scanner_clear_button").performClick()
        composeRule.waitForIdle()

        // 4. URL Inspector workflow (validation -> risky IP URL -> harmless URL -> clear)
        composeRule.onNodeWithTag("bottom_nav_url_inspector").performClick()
        composeRule.waitForIdle()
        scrollToTag("url_inspector_list", "url_inspect_button")
        composeRule.onNodeWithTag("url_inspect_button").performClick()
        composeRule.waitForIdle()
        scrollToText("url_inspector_list", "Please paste a URL to inspect its structure.")
        composeRule.onNodeWithText("Please paste a URL to inspect its structure.").assertIsDisplayed()

        scrollToTag("url_inspector_list", "url_inspector_input")
        composeRule.onNodeWithTag("url_inspector_input").performTextInput("http://198.51.100.42/verify")
        scrollToTag("url_inspector_list", "url_inspect_button")
        composeRule.onNodeWithTag("url_inspect_button").performClick()
        composeRule.waitForIdle()
        scrollToText("url_inspector_list", "OBSERVED URL COMPONENTS (FACTS)")
        composeRule.onNodeWithText("OBSERVED URL COMPONENTS (FACTS)").assertIsDisplayed()

        scrollToTag("url_inspector_list", "url_clear_button")
        composeRule.onNodeWithTag("url_clear_button").performClick()
        composeRule.waitForIdle()
        scrollToTag("url_inspector_list", "url_inspector_input")
        composeRule.onNodeWithTag("url_inspector_input").performTextInput("https://www.google.com")
        scrollToTag("url_inspector_list", "url_inspect_button")
        composeRule.onNodeWithTag("url_inspect_button").performClick()
        composeRule.waitForIdle()
        scrollToText("url_inspector_list", "SUSPECTED PATTERN RISKS")
        composeRule.onNodeWithText("SUSPECTED PATTERN RISKS").assertIsDisplayed()

        // 5. Cyber Academy — 8 lessons + knowledge check completion
        composeRule.onNodeWithTag("bottom_nav_academy").performClick()
        composeRule.waitForIdle()
        scrollToTag("academy_list", "academy_lesson_lesson_phishing")
        composeRule.onNodeWithTag("academy_lesson_lesson_phishing").performClick()
        composeRule.waitForIdle()
        scrollToTag("lesson_detail_list", "lesson_kc_option_1")
        composeRule.onNodeWithTag("lesson_kc_option_1").performClick()
        scrollToTag("lesson_detail_list", "lesson_check_answer_button")
        composeRule.onNodeWithTag("lesson_check_answer_button").performClick()
        composeRule.waitForIdle()
        scrollToTag("lesson_detail_list", "lesson_back_button")
        composeRule.onNodeWithTag("lesson_back_button").performClick()
        composeRule.waitForIdle()
        scrollToText("academy_list", "1 / 8")
        composeRule.onNodeWithText("1 / 8").assertIsDisplayed()

        // 6. Drawer navigation to Security Quiz — complete all 12 questions and restart
        composeRule.onNodeWithTag("topbar_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_nav_quiz").performScrollTo().performClick()
        composeRule.waitForIdle()

        val totalQuestions = EducationalContentRepository.quizQuestions.size
        assertEquals(12, totalQuestions)
        repeat(totalQuestions) {
            scrollToTag("quiz_list", "quiz_option_1")
            composeRule.onNodeWithTag("quiz_option_1").performClick()
            scrollToTag("quiz_list", "quiz_submit_button")
            composeRule.onNodeWithTag("quiz_submit_button").performClick()
            composeRule.waitForIdle()
            scrollToTag("quiz_list", "quiz_next_button")
            composeRule.onNodeWithTag("quiz_next_button").performClick()
            composeRule.waitForIdle()
        }
        scrollToTag("quiz_list", "quiz_restart_button")
        composeRule.onNodeWithTag("quiz_restart_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        scrollToText("quiz_list", "QUESTION 1 OF 12")
        composeRule.onNodeWithText("QUESTION 1 OF 12").assertIsDisplayed()

        // 7. Drawer navigation to History, TopBar About, and Settings
        composeRule.onNodeWithTag("topbar_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_nav_history").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No Scans Recorded This Session").assertIsDisplayed()

        composeRule.onNodeWithTag("topbar_about_button").performClick()
        composeRule.waitForIdle()
        scrollToText("about_list", "RESPONSIBLE AI & SECURITY DISCLAIMER")
        composeRule.onNodeWithText("RESPONSIBLE AI & SECURITY DISCLAIMER").assertIsDisplayed()

        composeRule.onNodeWithTag("topbar_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_nav_settings").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Toggle Reduce Motion switch
        scrollToTag("settings_list", "settings_reduce_motion_switch")
        composeRule.onNodeWithTag("settings_reduce_motion_switch").performClick()
        composeRule.waitForIdle()

        // 8. Language switcher to Urdu and back to English
        composeRule.onNodeWithTag("topbar_language_button").performClick()
        composeRule.waitForIdle()
        scrollToText("settings_list", "زبان، حرکت اور سیشن کی ترجیحات")
        composeRule.onNodeWithText("زبان، حرکت اور سیشن کی ترجیحات").assertIsDisplayed()

        composeRule.onNodeWithTag("topbar_language_button").performClick()
        composeRule.waitForIdle()
        scrollToText("settings_list", "Language, Accessibility & Session Controls")
        composeRule.onNodeWithText("Language, Accessibility & Session Controls").assertIsDisplayed()
    }

    @Test
    fun `audit end-to-end scanner, session history, reopen record, clear history, and lesson deep-link`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val testViewModel = ClickwiseViewModel(app, PlaceholderAiRepository())

        composeRule.setContent {
            ClickwiseTheme {
                ClickwiseApp(viewModel = testViewModel)
            }
        }
        dismissSplashIfPresent()

        // Load sample scholarship threat from Dashboard -> transitions to Scanner
        scrollToTag("dashboard_list", "dashboard_sample_sample_scholarship")
        composeRule.onNodeWithTag("dashboard_sample_sample_scholarship").performClick()
        composeRule.waitForIdle()

        // Run analysis and advance Android Main Looper for stage delays
        scrollToTag("scanner_list", "scanner_analyse_button")
        composeRule.onNodeWithTag("scanner_analyse_button").performClick()
        ShadowLooper.idleMainLooper(2, TimeUnit.SECONDS)
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 5000) {
            ShadowLooper.idleMainLooper(500, TimeUnit.MILLISECONDS)
            composeRule.onAllNodes(hasTestTag("result_list")).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify local URL pattern panel appears on result
        scrollToText("result_list", "LOCAL URL PATTERN ASSESSMENT")
        composeRule.onNodeWithText("LOCAL URL PATTERN ASSESSMENT").assertIsDisplayed()

        // Deep link from Result -> View Security Lesson -> opens Scholarship lesson
        scrollToTag("result_list", "result_view_lesson_button")
        composeRule.onNodeWithTag("result_view_lesson_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Identifying Fake Scholarship Offers").assertIsDisplayed()

        // Navigate back to Academy list via ALL LESSONS button
        scrollToTag("lesson_detail_list", "lesson_back_button")
        composeRule.onNodeWithTag("lesson_back_button").performClick()
        composeRule.waitForIdle()

        // Navigate to Session History and verify 1 record was saved
        composeRule.onNodeWithTag("topbar_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_nav_history").performClick()
        composeRule.waitForIdle()

        val recordId = testViewModel.sessionHistory.value.first().id
        scrollToTag("history_list", "history_item_$recordId")
        composeRule.onNodeWithTag("history_item_$recordId").performClick()
        composeRule.waitForIdle()

        // Reopening history item displays Result view again
        composeRule.onNodeWithTag("result_list").assertIsDisplayed()

        // Go back to History and clear session history
        composeRule.onNodeWithTag("topbar_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_nav_history").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("history_clear_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No Scans Recorded This Session").assertIsDisplayed()
    }

    @Test
    fun `audit result view clipboard copy, expandable findings, and print button`() {
        val sampleResult = MessageAnalysisResult(
            id = "audit-1",
            shortLabel = "Fake Scholarship",
            shortLabelUr = "جعلی اسکالرشپ",
            analysisType = AnalysisType.MESSAGE_AND_URL,
            timestampEpochMs = 1700000000000L,
            riskScore = 88,
            riskLevel = RiskLevel.CRITICAL,
            summary = "Suspicious scholarship announcement requesting OTP.",
            urduSummary = "مشکوک اسکالرشپ پیغام جس میں او ٹی پی مانگا گیا ہے۔",
            suspiciousPhrases = listOf("expires tonight", "OTP"),
            findings = listOf(
                AnalysisFinding(
                    title = "OTP Credential Request",
                    titleUr = "او ٹی پی کا مطالبہ",
                    severity = FindingSeverity.CRITICAL,
                    evidence = "Submit OTP",
                    explanation = "Never share OTP codes.",
                    explanationUr = "او ٹی پی شیئر نہ کریں۔",
                    detailedExplanation = "Detailed OTP explanation.",
                    detailedExplanationUr = "تفصیلی وضاحت۔"
                )
            ),
            redFlags = listOf("Artificial urgency"),
            redFlagsUr = listOf("مصنوعی عجلت"),
            positiveSignals = emptyList(),
            positiveSignalsUr = emptyList(),
            recommendedActions = listOf(
                RecommendedAction(
                    priority = ActionPriority.IMMEDIATE,
                    title = "Do not open link",
                    titleUr = "لنک نہ کھولیں",
                    description = "Verify independently.",
                    descriptionUr = "آزادانہ تصدیق کریں۔"
                )
            ),
            uncertainties = listOf("Sender identity unverified."),
            uncertaintiesUr = listOf("شناخت غیر مصدقہ ہے۔"),
            urlInspection = UrlInspectorEngine.inspect("http://hec-scholarship-claim.xyz/verify"),
            isPlaceholderMode = false
        )

        var analyzeAnotherCalled = false
        composeRule.setContent {
            ClickwiseTheme {
                AnalysisResultView(
                    result = sampleResult,
                    language = AppLanguage.EN,
                    reduceMotion = true,
                    onAnalyzeAnother = { analyzeAnotherCalled = true },
                    onToggleLanguage = {},
                    onOpenLesson = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Expand finding row
        scrollToTag("result_list", "result_finding_0")
        composeRule.onNodeWithTag("result_finding_0").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Detailed OTP explanation.").assertIsDisplayed()

        // Copy report to Android ClipboardManager and verify content
        scrollToTag("result_list", "result_copy_report_button")
        composeRule.onNodeWithTag("result_copy_report_button").performClick()
        composeRule.waitForIdle()
        val clipboard = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copied = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
        assertTrue(copied.contains("CLICKWISE — SECURITY ASSESSMENT REPORT"))
        assertTrue(copied.contains("88 / 100"))
        assertTrue(copied.contains("Gemini Developer API via Firebase AI Logic"))

        // Invoke Print / Save PDF button safely
        scrollToTag("result_list", "result_print_report_button")
        composeRule.onNodeWithTag("result_print_report_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // Verify Analyse Another triggers callback
        scrollToTag("result_list", "result_analyze_another_button")
        composeRule.onNodeWithTag("result_analyze_another_button").performClick()
        composeRule.waitForIdle()
        assertTrue(analyzeAnotherCalled)
    }
}
