package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.analyzer.GeminiResponseParser
import com.example.domain.analyzer.PlaceholderAiRepository
import com.example.domain.analyzer.UrlInspectorEngine
import com.example.domain.model.AnalysisType
import com.example.domain.model.AppLanguage
import com.example.domain.model.MessageAnalysisRequest
import com.example.domain.model.RiskLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ClickwiseAnalyzerTest {

    @Test
    fun `firebase recaptcha site key resource is provisioned`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resId = context.resources.getIdentifier("recaptcha_site_key", "string", context.packageName)
        assertTrue("recaptcha_site_key must exist after Firebase AI Logic provisioning", resId != 0)
        assertTrue(context.getString(resId).isNotBlank())
    }

    @Test
    fun `suspicious scholarship message scores high or critical with findings`() = runTest {
        val repo = PlaceholderAiRepository()
        val result = repo.analyzeMessage(
            MessageAnalysisRequest(
                message = "CONGRATULATIONS! Selected for Merit Scholarship. Expires today! Submit CNIC, bank account, and OTP at http://hec-scholarship-claim.xyz/verify",
                optionalUrl = "http://hec-scholarship-claim.xyz/verify",
                language = AppLanguage.EN
            )
        )
        assertTrue(result.riskScore >= 50)
        assertTrue(result.riskLevel == RiskLevel.HIGH || result.riskLevel == RiskLevel.CRITICAL)
        assertTrue(result.findings.isNotEmpty())
        assertTrue(result.recommendedActions.isNotEmpty())
    }

    @Test
    fun `harmless classroom timetable message scores low without false fraud labels`() = runTest {
        val repo = PlaceholderAiRepository()
        val result = repo.analyzeMessage(
            MessageAnalysisRequest(
                message = "Reminder: CS-301 Software Engineering lecture tomorrow is shifted from Room 204 to Lab 3 at 10:30 AM.",
                language = AppLanguage.EN
            )
        )
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertTrue(result.riskScore < 25)
        assertTrue(result.findings.isEmpty())
        assertTrue(result.positiveSignals.isNotEmpty())
    }

    @Test
    fun `url inspector detects ip host, http scheme, shorteners, punycode, and at-symbol authority`() {
        val ipInspection = UrlInspectorEngine.inspect("http://198.51.100.42/redelivery/confirm")
        assertTrue(ipInspection.isValidFormat)
        assertTrue(ipInspection.patternRiskScore >= 50)
        assertTrue(ipInspection.riskIndicators.any { it.title.contains("IP Address") })

        val atInspection = UrlInspectorEngine.inspect("http://portal@reward-claim-center.click/claim")
        assertTrue(atInspection.isValidFormat)
        assertTrue(atInspection.riskIndicators.any { it.title.contains("@ Symbol") })

        val shortenerInspection = UrlInspectorEngine.inspect("https://bit.ly/3xFakeInternOffer")
        assertTrue(shortenerInspection.isValidFormat)
        assertTrue(shortenerInspection.riskIndicators.any { it.title.contains("Shortener") })

        val punycodeInspection = UrlInspectorEngine.inspect("https://xn--univrsty-80a.edu.example.xyz/login")
        assertTrue(punycodeInspection.isValidFormat)
        assertTrue(punycodeInspection.riskIndicators.any { it.title.contains("Punycode") })

        val normalUrl = UrlInspectorEngine.inspect("https://www.google.com")
        assertTrue(normalUrl.isValidFormat)
        assertEquals(0, normalUrl.patternRiskScore)
        assertEquals(RiskLevel.LOW, normalUrl.patternRiskLevel)
        assertTrue(normalUrl.riskIndicators.isEmpty())
    }

    @Test
    fun `url inspector handles empty and malformed inputs gracefully`() {
        val empty = UrlInspectorEngine.inspect("   ")
        assertFalse(empty.isValidFormat)

        val malformed = UrlInspectorEngine.inspect("not a valid host")
        assertFalse(malformed.isValidFormat)
    }

    @Test
    fun `gemini response parser parses structured json and merges local url indicators`() {
        val sampleJson = """
            {
              "riskScore": 88,
              "riskLevel": "CRITICAL",
              "shortLabel": "Fake Scholarship Request",
              "shortLabelUr": "جعلی اسکالرشپ پیغام",
              "summary": "Multiple phishing and sensitive credential requests detected.",
              "urduSummary": "اس پیغام میں فشنگ اور ذاتی معلومات کے مطالبے کی متعدد علامات موجود ہیں۔",
              "suspiciousPhrases": ["expires tonight", "submit CNIC and OTP"],
              "findings": [
                {
                  "title": "OTP Credential Request",
                  "titleUr": "او ٹی پی کوڈ کا مطالبہ",
                  "severity": "CRITICAL",
                  "evidence": "submit CNIC and OTP",
                  "explanation": "Legitimate portals never request OTPs via unsolicited links.",
                  "explanationUr": "مستند ادارے غیر متوقع لنکس کے ذریعے او ٹی پی نہیں مانگتے۔",
                  "detailedExplanation": "OTPs protect financial and identity accounts.",
                  "detailedExplanationUr": "او ٹی پی آپ کے اکاؤنٹ کی حفاظت کے لیے ہوتا ہے۔"
                }
              ],
              "redFlags": ["Artificial urgency", "OTP request"],
              "redFlagsUr": ["مصنوعی عجلت", "او ٹی پی کا مطالبہ"],
              "positiveSignals": [],
              "positiveSignalsUr": [],
              "recommendedActions": [
                {
                  "priority": "IMMEDIATE",
                  "title": "Do not open link",
                  "titleUr": "لنک نہ کھولیں",
                  "description": "Verify directly on the official university website.",
                  "descriptionUr": "یونیورسٹی کی اصل ویب سائٹ سے تصدیق کریں۔"
                }
              ],
              "uncertainties": ["Sender identity could not be independently verified."],
              "uncertaintiesUr": ["بھیجنے والے کی اصل شناخت کی تصدیق ممکن نہیں۔"]
            }
        """.trimIndent()

        val urlInspection = UrlInspectorEngine.inspect("http://hec-scholarship-claim.xyz/verify")
        val parsed = GeminiResponseParser.parse(
            jsonString = sampleJson,
            analysisType = AnalysisType.MESSAGE_AND_URL,
            urlInspection = urlInspection
        )

        assertFalse(parsed.isPlaceholderMode)
        assertEquals(88, parsed.riskScore)
        assertEquals(RiskLevel.CRITICAL, parsed.riskLevel)
        assertTrue(parsed.findings.any { it.title == "OTP Credential Request" })
        assertTrue(parsed.findings.any { it.title.startsWith("Local URL Check:") })
        assertEquals(1, parsed.recommendedActions.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `gemini response parser rejects missing required fields`() {
        GeminiResponseParser.parse(
            jsonString = "{}",
            analysisType = AnalysisType.MESSAGE_ONLY,
            urlInspection = null
        )
    }

    @Test
    fun `gemini response parser enforces score-to-risk-level consistency`() {
        val mismatchedJson = """
            {
              "riskScore": 15,
              "riskLevel": "CRITICAL",
              "summary": "No obvious warning signs detected."
            }
        """.trimIndent()
        val parsed = GeminiResponseParser.parse(
            jsonString = mismatchedJson,
            analysisType = AnalysisType.MESSAGE_ONLY,
            urlInspection = null
        )
        assertEquals(RiskLevel.LOW, parsed.riskLevel)
        assertTrue(parsed.recommendedActions.isNotEmpty())
        assertTrue(parsed.uncertainties.isNotEmpty())
    }
}
