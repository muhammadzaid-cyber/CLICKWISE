package com.example.domain.analyzer

import com.example.domain.model.ActionPriority
import com.example.domain.model.AnalysisFinding
import com.example.domain.model.AnalysisType
import com.example.domain.model.AppLanguage
import com.example.domain.model.FindingSeverity
import com.example.domain.model.MessageAnalysisRequest
import com.example.domain.model.MessageAnalysisResult
import com.example.domain.model.RecommendedAction
import com.example.domain.model.RiskLevel
import com.example.domain.model.UrlInspectionResult
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.UUID

interface AiRepository {
    suspend fun analyzeMessage(
        request: MessageAnalysisRequest,
        onStageChanged: (AnalysisStage) -> Unit = {}
    ): MessageAnalysisResult
}

enum class AnalysisStage {
    PREPARING,
    INSPECTING_MESSAGE,
    IDENTIFYING_WARNINGS,
    GENERATING_ASSESSMENT,
    FINALIZING
}

/**
 * Placeholder AiRepository used when Firebase AI Logic setup was declined.
 * Performs deterministic local heuristic + URL pattern inspection on the actual submitted
 * message/URL and returns clearly labeled placeholder AI assessment responses without
 * making direct Gemini REST calls or embedding any API key.
 */
class PlaceholderAiRepository : AiRepository {

    private data class PatternRule(
        val id: String,
        val keywords: List<String>,
        val weight: Int,
        val severity: FindingSeverity,
        val title: String,
        val titleUr: String,
        val redFlag: String,
        val redFlagUr: String,
        val explanation: String,
        val explanationUr: String,
        val detailedExplanation: String,
        val detailedExplanationUr: String
    )

    private val rules = listOf(
        PatternRule(
            id = "credentials_otp",
            keywords = listOf("otp", "pin", "password", "cvv", "mpin", "verification code", "6-digit code", "passcode"),
            weight = 34,
            severity = FindingSeverity.CRITICAL,
            title = "Request for OTP, PIN, or Credentials",
            titleUr = "او ٹی پی، پن یا پاس ورڈ کا مطالبہ",
            redFlag = "Requests private authentication code or password",
            redFlagUr = "خفیہ کوڈ یا پاس ورڈ طلب کیا جا رہا ہے",
            explanation = "Legitimate organizations never ask you to share an OTP, PIN, or password via messages or forms.",
            explanationUr = "مستند ادارے کبھی بھی پیغام یا لنک کے ذریعے آپ کا او ٹی پی، پن یا پاس ورڈ نہیں مانگتے۔",
            detailedExplanation = "One-Time Passwords (OTPs) and banking PINs authorize real transactions and account takeovers. Any message asking you to forward, reply with, or enter an OTP on an unsolicited page is a severe account-compromise warning sign.",
            detailedExplanationUr = "او ٹی پی اور بینک پن آپ کے اکاؤنٹ تک براہِ راست رسائی دیتے ہیں۔ کسی بھی غیر متوقع پیغام میں ان کا مطالبہ سنگین خطرے کی علامت ہے۔"
        ),
        PatternRule(
            id = "bank_financial",
            keywords = listOf("bank account", "iban", "account number", "atm card", "debit card", "credit card", "easypaisa", "jazzcash", "bank details", "cnic", "processing fee", "registration fee", "customs fee", "clearance fee", "transfer rs", "pkr"),
            weight = 28,
            severity = FindingSeverity.HIGH,
            title = "Financial Credentials or Advance-Fee Request",
            titleUr = "مالی معلومات یا پیشگی فیس کا مطالبہ",
            redFlag = "Requests banking details or advance payment",
            redFlagUr = "بینک تفصیلات یا پیشگی رقم کا مطالبہ",
            explanation = "Asking for bank account numbers, CNIC details, or upfront processing fees is a classic advance-fee or phishing tactic.",
            explanationUr = "بینک اکاؤنٹ، شناختی کارڈ یا پیشگی فیس کا مطالبہ دھوکہ دہی اور فشنگ کی نمایاں علامت ہے۔",
            detailedExplanation = "Legitimate scholarships, internships, and parcel services do not require sudden personal fee transfers to personal wallets or unverified forms before granting an award or delivering a package.",
            detailedExplanationUr = "مستند اسکالرشپس، انٹرن شپس یا کوریئر ادارے ذاتی والٹ میں اچانک فیس جمع کرانے یا لنک پر بینک تفصیلات دینے کا مطالبہ نہیں کرتے۔"
        ),
        PatternRule(
            id = "urgency",
            keywords = listOf("urgent", "immediately", "within 24 hours", "within 2 hours", "expires today", "last chance", "account suspended", "blocked", "final warning", "act now", "limited slots", "forfeited", "deadline tonight"),
            weight = 20,
            severity = FindingSeverity.HIGH,
            title = "Artificial Urgency or Threat Pressure",
            titleUr = "مصنوعی عجلت یا اکاؤنٹ بند ہونے کی دھمکی",
            redFlag = "Artificial time pressure to rush your decision",
            redFlagUr = "فوری فیصلے کے لیے مصنوعی دباؤ",
            explanation = "Scammers use tight deadlines or threats of suspension so victims click before verifying.",
            explanationUr = "دھوکہ باز مصنوعی عجلت یا اکاؤنٹ معطلی کا خوف پیدا کرتے ہیں تاکہ صارف تصدیق کیے بغیر لنک پر کلک کر دے۔",
            detailedExplanation = "Emotional pressure—such as claiming a scholarship expires tonight or a bank account will be locked in 2 hours—is engineered to bypass critical thinking and independent verification.",
            detailedExplanationUr = "یہ دباؤ جان بوجھ کر ڈالا جاتا ہے تاکہ آپ سوچنے یا سرکاری ویب سائٹ سے تصدیق کرنے کا وقت نہ لیں۔"
        ),
        PatternRule(
            id = "scholarship_prize_lure",
            keywords = listOf("congratulations", "selected for", "won", "winner", "prize", "lottery", "grant", "stipend", "scholarship", "lucky draw", "unclaimed"),
            weight = 20,
            severity = FindingSeverity.MEDIUM,
            title = "Unexpected Reward or Scholarship Lure",
            titleUr = "غیر متوقع انعام یا اسکالرشپ کی پیشکش",
            redFlag = "Unsolicited reward, prize, or selection claim",
            redFlagUr = "بن مانگے انعام یا انتخاب کا دعویٰ",
            explanation = "Unsolicited messages announcing prizes or scholarships you never applied for are commonly used as social engineering bait.",
            explanationUr = "جن انعامات یا اسکالرشپس کے لیے آپ نے درخواست نہ دی ہو، ان کا پیغام عموماً جال بچھانے کے لیے ہوتا ہے۔",
            detailedExplanation = "Always cross-check university scholarships directly on your university's official portal or HEC announcements rather than trusting forwarded messages.",
            detailedExplanationUr = "کسی بھی اسکالرشپ کی تصدیق ہمیشہ یونیورسٹی کے سرکاری پورٹل یا ایچ ای سی کی اصل ویب سائٹ سے کریں۔"
        ),
        PatternRule(
            id = "job_internship_lure",
            keywords = listOf("work from home", "daily income", "guaranteed job", "remote internship", "no interview", "hr department", "offer letter", "salary", "part-time", "whatsapp me"),
            weight = 18,
            severity = FindingSeverity.MEDIUM,
            title = "Suspicious Employment or Internship Claim",
            titleUr = "مشکوک ملازمت یا انٹرن شپ کی پیشکش",
            redFlag = "Unverified job or instant internship offer",
            redFlagUr = "غیر مصدقہ ملازمت یا فوری انٹرن شپ کا دعویٰ",
            explanation = "Instant job offers with no interview, unusually high pay, or requests to pay for onboarding are common student scams.",
            explanationUr = "بغیر انٹرویو فوری نوکری، غیر معمولی تنخواہ یا رجسٹریشن فیس کا مطالبہ طلبہ کو نشانہ بنانے کا عام طریقہ ہے۔",
            detailedExplanation = "Verify employer identities through official company career pages and never pay training, badge, or equipment fees to start an internship.",
            detailedExplanationUr = "کسی بھی ملازمت یا انٹرن شپ کی تصدیق کمپنی کی اصل ویب سائٹ سے کریں اور شمولیت کے لیے کوئی فیس ادا نہ کریں۔"
        ),
        PatternRule(
            id = "delivery_impersonation",
            keywords = listOf("parcel", "package", "courier", "delivery failed", "incomplete address", "customs", "redelivery", "shipment"),
            weight = 18,
            severity = FindingSeverity.MEDIUM,
            title = "Delivery or Courier Notification Pattern",
            titleUr = "مشکوک پارسل یا کوریئر پیغام",
            redFlag = "Unsolicited parcel or redelivery fee prompt",
            redFlagUr = "غیر متوقع پارسل یا دوبارہ ترسیل کی فیس",
            explanation = "Fake delivery texts claim an address is missing or customs fees are due to lure users into payment phishing pages.",
            explanationUr = "جعلی کوریئر پیغامات میں پتہ نامکمل ہونے یا کسٹم فیس کا بہانہ بنا کر لنک کھولنے پر آمادہ کیا جاتا ہے۔",
            detailedExplanation = "Track shipments only by typing the official courier website address manually and entering your original tracking number.",
            detailedExplanationUr = "پارسل ٹریک کرنے کے لیے کوریئر کمپنی کی اصل ویب سائٹ خود ٹائپ کریں، پیغام میں موجود لنک استعمال نہ کریں۔"
        )
    )

    override suspend fun analyzeMessage(
        request: MessageAnalysisRequest,
        onStageChanged: (AnalysisStage) -> Unit
    ): MessageAnalysisResult {
        onStageChanged(AnalysisStage.PREPARING)
        delay(220)

        onStageChanged(AnalysisStage.INSPECTING_MESSAGE)
        delay(260)

        val message = request.message.trim()
        val lower = message.lowercase(Locale.US)
        val extractedUrl = request.optionalUrl.trim().ifBlank {
            UrlInspectorEngine.extractFirstUrl(message).orEmpty()
        }
        val urlResult: UrlInspectionResult? = if (extractedUrl.isNotBlank()) {
            UrlInspectorEngine.inspect(extractedUrl)
        } else {
            null
        }

        onStageChanged(AnalysisStage.IDENTIFYING_WARNINGS)
        delay(260)

        val findings = mutableListOf<AnalysisFinding>()
        val redFlags = mutableListOf<String>()
        val redFlagsUr = mutableListOf<String>()
        val suspiciousPhrases = mutableListOf<String>()
        var score = 0

        for (rule in rules) {
            val hits = rule.keywords.filter { lower.contains(it) }
            if (hits.isNotEmpty()) {
                score += rule.weight
                suspiciousPhrases.addAll(hits.take(3))
                redFlags += rule.redFlag
                redFlagsUr += rule.redFlagUr
                findings += AnalysisFinding(
                    title = rule.title,
                    titleUr = rule.titleUr,
                    severity = rule.severity,
                    evidence = "Matched phrase(s): ${hits.joinToString(", ") { "\"$it\"" }}",
                    explanation = rule.explanation,
                    explanationUr = rule.explanationUr,
                    detailedExplanation = rule.detailedExplanation,
                    detailedExplanationUr = rule.detailedExplanationUr
                )
            }
        }

        if (urlResult != null && urlResult.riskIndicators.isNotEmpty()) {
            val urlContribution = (urlResult.patternRiskScore * 0.45).toInt().coerceAtLeast(12)
            score += urlContribution
            urlResult.riskIndicators.forEach { indicator ->
                findings += AnalysisFinding(
                    title = "URL Pattern: ${indicator.title}",
                    titleUr = "لنک پیٹرن: ${indicator.titleUr}",
                    severity = indicator.severity,
                    evidence = indicator.observation,
                    explanation = indicator.explanation,
                    explanationUr = indicator.explanationUr,
                    detailedExplanation = "Local URL inspection observed: ${indicator.observation}. ${indicator.explanation}",
                    detailedExplanationUr = "مقامی لنک معائنے میں مشاہدہ: ${indicator.observation}۔ ${indicator.explanationUr}"
                )
            }
            redFlags += "Contains link with suspicious structural patterns"
            redFlagsUr += "پیغام میں مشکوک ساخت والا لنک شامل ہے"
        }

        onStageChanged(AnalysisStage.GENERATING_ASSESSMENT)
        delay(240)

        val clampedScore = score.coerceIn(0, 100)
        val riskLevel = RiskLevel.fromScore(clampedScore)

        val positiveSignals = mutableListOf<String>()
        val positiveSignalsUr = mutableListOf<String>()
        if (findings.isEmpty()) {
            positiveSignals += "No common credential, OTP, advance-fee, or urgency lure keywords were detected in the submitted text."
            positiveSignalsUr += "درج کردہ پیغام میں پاس ورڈ، او ٹی پی، پیشگی فیس یا مصنوعی عجلت جیسے عام خطرناک الفاظ نہیں ملے۔"
        }
        if (urlResult == null) {
            positiveSignals += "No external web link was detected in the submission."
            positiveSignalsUr += "اس پیغام میں کوئی بیرونی ویب لنک موجود نہیں پایا گیا۔"
        } else if (urlResult.riskIndicators.isEmpty()) {
            positiveSignals += "No common suspicious URL structural patterns were observed locally (note: this does not establish site safety)."
            positiveSignalsUr += "لنک میں عام مشکوک پیٹرن نہیں ملے (تاہم یہ ویب سائٹ کے محفوظ ہونے کی ضمانت نہیں ہے)۔"
        }

        val recommendedActions = buildActions(riskLevel, urlResult != null, lower)
        val urduActions = recommendedActions.map { it.descriptionUr }

        val uncertainties = mutableListOf(
            "Placeholder Mode: Live Gemini cloud inference is unavailable because Firebase setup was declined.",
            "The sender's real-world identity, phone number ownership, and institutional authority could not be independently verified.",
            "Absence of detected warning signs does not prove that a message or website is legitimate."
        )
        val uncertaintiesUr = listOf(
            "پلیس ہولڈر موڈ: فائربیس سیٹ اپ مسترد ہونے کی وجہ سے لائیو جیمینائی کلاؤڈ تجزیہ فعال نہیں ہے۔",
            "بھیجنے والے کی اصل شناخت یا ادارے کی ملکیت کی آزادانہ تصدیق ممکن نہیں ہے۔",
            "خطرناک علامات نہ ملنے کا مطلب یہ نہیں کہ پیغام یا ویب سائٹ سو فیصد مستند ہے۔"
        )

        val (summaryEn, summaryUr) = when (riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH -> Pair(
                "Multiple high-risk social engineering or phishing indicators were detected in this submission. Do not click links, send money, or share sensitive details.",
                "اس پیغام میں فشنگ یا دھوکہ دہی کی متعدد نمایاں علامات پائی گئی ہیں۔ کسی لنک پر کلک نہ کریں، رقم نہ بھیجیں اور ذاتی معلومات شیئر نہ کریں۔"
            )
            RiskLevel.MEDIUM -> Pair(
                "Some cautionary indicators were identified. Treat the message carefully and verify the claim through an official independent channel before acting.",
                "اس پیغام میں کچھ محتاط رہنے والی علامات پائی گئی ہیں۔ کوئی بھی قدم اٹھانے سے پہلے سرکاری ذرائع سے تصدیق کریں۔"
            )
            RiskLevel.LOW -> Pair(
                "No obvious scam keywords or structural warning patterns were detected. Still, absence of detected warning signs does not confirm the sender is authentic.",
                "اس پیغام میں دھوکہ دہی کی کوئی واضح علامت نہیں ملی، تاہم صرف اس بنیاد پر بھیجنے والے کو مصدقہ نہیں سمجھا جا سکتا۔"
            )
        }

        val analysisType = when {
            message.isBlank() && urlResult != null -> AnalysisType.URL_ONLY
            urlResult != null -> AnalysisType.MESSAGE_AND_URL
            else -> AnalysisType.MESSAGE_ONLY
        }

        val shortLabelEn = buildShortLabel(message, extractedUrl, riskLevel)
        val shortLabelUr = when (riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH -> "اعلیٰ خطرے والا پیغام (${shortLabelEn.take(24)})"
            RiskLevel.MEDIUM -> "محتاط معائنہ (${shortLabelEn.take(24)})"
            RiskLevel.LOW -> "کم خطرے والا پیغام (${shortLabelEn.take(24)})"
        }

        onStageChanged(AnalysisStage.FINALIZING)
        delay(180)

        return MessageAnalysisResult(
            id = UUID.randomUUID().toString(),
            shortLabel = shortLabelEn,
            shortLabelUr = shortLabelUr,
            analysisType = analysisType,
            timestampEpochMs = System.currentTimeMillis(),
            riskScore = clampedScore,
            riskLevel = riskLevel,
            summary = summaryEn,
            urduSummary = summaryUr,
            suspiciousPhrases = suspiciousPhrases.distinct(),
            findings = findings,
            redFlags = redFlags.distinct(),
            redFlagsUr = redFlagsUr.distinct(),
            positiveSignals = positiveSignals,
            positiveSignalsUr = positiveSignalsUr,
            recommendedActions = recommendedActions,
            uncertainties = uncertainties,
            uncertaintiesUr = uncertaintiesUr,
            urlInspection = urlResult,
            isPlaceholderMode = true
        )
    }

    private fun buildShortLabel(message: String, url: String, riskLevel: RiskLevel): String {
        val cleaned = message.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        return when {
            cleaned.isNotEmpty() -> {
                val words = cleaned.split(Regex("\\s+")).take(5).joinToString(" ")
                if (cleaned.length > words.length) "$words..." else words
            }
            url.isNotEmpty() -> "URL: ${url.take(32)}"
            else -> "${riskLevel.name} Assessment"
        }
    }

    private fun buildActions(
        riskLevel: RiskLevel,
        hasUrl: Boolean,
        lowerMessage: String
    ): List<RecommendedAction> {
        val actions = mutableListOf<RecommendedAction>()
        if (riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL) {
            if (hasUrl) {
                actions += RecommendedAction(
                    priority = ActionPriority.IMMEDIATE,
                    title = "Do not open the link in the message",
                    titleUr = "پیغام میں موجود لنک ہرگز نہ کھولیں",
                    description = "Avoid tapping links from unsolicited announcements. Open the official organization website manually in your browser instead.",
                    descriptionUr = "غیر متوقع پیغامات میں موجود لنکس پر کلک نہ کریں، بلکہ ادارے کی اصل ویب سائٹ خود براؤزر میں کھولیں۔"
                )
            }
            actions += RecommendedAction(
                priority = ActionPriority.IMMEDIATE,
                title = "Never share OTPs, passwords, or bank details",
                titleUr = "او ٹی پی، پاس ورڈ یا بینک تفصیلات ہرگز شیئر نہ کریں",
                description = "Do not send banking details, CNIC photos, passwords, or verification codes. If already shared, contact your bank immediately via its official helpline.",
                descriptionUr = "اپنا او ٹی پی، پاس ورڈ، شناختی کارڈ یا بینک اکاؤنٹ کی تفصیل کسی کو نہ دیں۔ اگر معلومات دے دی ہیں تو فوراً اپنے بینک کی سرکاری ہیلپ لائن پر رابطہ کریں۔"
            )
            actions += RecommendedAction(
                priority = ActionPriority.VERIFY,
                title = "Verify through official institutional channels",
                titleUr = "ادارے کے سرکاری ذرائع سے تصدیق کریں",
                description = "Confirm scholarship, job, or account alerts directly through your university registrar, official employer website, or printed bank helpline.",
                descriptionUr = "اسکالرشپ، ملازمت یا بینک الرٹ کی تصدیق براہِ راست یونیورسٹی یا متعلقہ ادارے کی سرکاری ویب سائٹ اور آفیشل نمبر سے کریں۔"
            )
        } else if (riskLevel == RiskLevel.MEDIUM) {
            actions += RecommendedAction(
                priority = ActionPriority.VERIFY,
                title = "Independently confirm the sender",
                titleUr = "بھیجنے والے کی آزادانہ تصدیق کریں",
                description = "Check the claim on an official portal or by calling a known official number before responding or clicking.",
                descriptionUr = "جواب دینے یا کلک کرنے سے پہلے سرکاری پورٹل یا معلوم فون نمبر سے تصدیق کریں۔"
            )
            if (lowerMessage.contains("fee") || lowerMessage.contains("payment")) {
                actions += RecommendedAction(
                    priority = ActionPriority.IMMEDIATE,
                    title = "Pause before making any payment",
                    titleUr = "کسی بھی ادائیگی سے پہلے رکیں",
                    description = "Do not transfer money to personal mobile wallets or unverified accounts.",
                    descriptionUr = "ذاتی موبائل والٹ یا غیر مصدقہ اکاؤنٹ میں رقم منتقل نہ کریں۔"
                )
            }
        } else {
            actions += RecommendedAction(
                priority = ActionPriority.RECOMMENDED,
                title = "Keep standard verification habits",
                titleUr = "معمول کی احتیاطی تدابیر برقرار رکھیں",
                description = "Even when a message shows no obvious warning keywords, double-check unexpected requests with a known contact or official schedule.",
                descriptionUr = "اگرچہ پیغام میں واضح خطرناک علامات نہیں ملیں، پھر بھی غیر متوقع درخواستوں کی تصدیق معروف ذرائع سے کریں۔"
            )
        }
        return actions
    }
}
