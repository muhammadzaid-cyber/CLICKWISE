package com.example.data

import com.example.domain.model.AppLanguage
import com.example.domain.model.RiskLevel

object ClickwiseStrings {

    fun tagline(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "کلک کرنے سے پہلے سوچیں۔" else "THINK BEFORE YOU CLICK."

    // Navigation titles
    fun navDashboard(lang: AppLanguage) = if (lang == AppLanguage.UR) "ڈیش بورڈ" else "Dashboard"
    fun navScanner(lang: AppLanguage) = if (lang == AppLanguage.UR) "پیغام اسکینر" else "Message Scanner"
    fun navUrlInspector(lang: AppLanguage) = if (lang == AppLanguage.UR) "لنک معائنہ کار" else "URL Inspector"
    fun navHistory(lang: AppLanguage) = if (lang == AppLanguage.UR) "اسکین ہسٹری" else "Scan History"
    fun navAcademy(lang: AppLanguage) = if (lang == AppLanguage.UR) "سائبر اکیڈمی" else "Cyber Academy"
    fun navQuiz(lang: AppLanguage) = if (lang == AppLanguage.UR) "سیکیورٹی کوئز" else "Security Quiz"
    fun navAbout(lang: AppLanguage) = if (lang == AppLanguage.UR) "تعارف و اعتماد" else "About CLICKWISE"
    fun navSettings(lang: AppLanguage) = if (lang == AppLanguage.UR) "ترتیبات" else "Settings"

    // Honest system status
    fun statusEngineBadge(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "جیمینائی AI منسلک • ایپ چیک فعال"
        else "GEMINI AI CONNECTED • APP CHECK ACTIVE"

    // Placeholder / Firebase declined notice
    fun placeholderNoticeTitle(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "اے آئی فیچرز کے لیے فائربیس درکار ہے (پلیس ہولڈر موڈ)"
        else "AI Features Require Firebase — Placeholder Analysis Mode"

    fun placeholderNoticeBody(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "فائربیس سیٹ اپ مسترد ہونے کی وجہ سے لائیو جیمینائی کلاؤڈ اے آئی منسلک نہیں ہے۔ فی الحال نتائج مقامی پیٹرن جانچ اور واضح طور پر لیبل شدہ پلیس ہولڈر تجزیے پر مبنی ہیں۔"
        else "Gemini cloud AI is disconnected because Firebase setup was declined. Results below use local heuristic checks and clearly labeled placeholder AI commentary rather than live Gemini inference."

    // Dashboard
    fun heroHeadline(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "آپ کا اگلا کلک دوسرے خیال کا مستحق ہے۔"
        else "Your next click deserves a second thought."

    fun heroSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "مشکوک پیغامات کی پہچان کریں، ڈیجیٹل خطرات کو سمجھیں اور بہتر سیکیورٹی فیصلے کریں۔"
        else "Identify suspicious messages, understand digital threats, and make smarter security decisions with structured security analysis."

    fun startAnalysis(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "تجزیہ شروع کریں" else "START ANALYSIS"

    fun exploreAcademy(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "سائبر اکیڈمی دیکھیں" else "EXPLORE CYBER ACADEMY"

    fun quickScannerTitle(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "فوری پیغام اسکینر" else "Quick Scanner"

    fun quickScannerDesc(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "کوئی بھی مشکوک اسکالرشپ، ملازمت، پارسل یا بینک پیغام فوری طور پر جانچیں۔"
        else "Inspect suspicious scholarship, job, delivery, or payment announcements in seconds."

    fun sampleThreatsTitle(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "تعلیمی خطرات کی مثالیں (ڈیمو)" else "Sample Threat Demonstrations"

    fun sampleDemoBadge(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "فرضی تعلیمی مثال" else "EDUCATIONAL DEMO"

    fun awarenessTitle(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "بنیادی سیکیورٹی رہنمائی" else "Security Awareness"

    // Scanner
    fun scannerHeadline(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "رابطے سے پہلے معائنہ کریں۔"
        else "Inspect before you interact."

    fun scannerSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "کوئی مشکوک پیغام یہاں پیسٹ کریں تاکہ CLICKWISE اس کی خطرناک علامات واضح کرے۔"
        else "Paste a suspicious message and let CLICKWISE explain the warning signs."

    fun messagePlaceholder(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "یہاں مشکوک اسکالرشپ، انٹرن شپ، ادائیگی، پارسل یا سوشل میڈیا پیغام پیسٹ کریں..."
        else "Paste a suspicious scholarship, internship, payment, delivery or social media message here..."

    fun optionalUrlLabel(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "اختیاری مشکوک لنک (URL)" else "Optional Suspicious URL"

    fun analyseButton(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "پیغام کا تجزیہ کریں" else "ANALYSE MESSAGE"

    fun clearButton(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "صاف کریں" else "Clear"

    fun pasteButton(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "پیسٹ" else "Paste"

    fun privacyNotice(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "آپ کا درج کردہ مواد AI تجزیے کے لیے Firebase AI Logic کے ذریعے Google کی Gemini سروس کو بھیجا جاتا ہے۔ براہِ کرم پاس ورڈ، او ٹی پی، بینک تفصیلات یا نجی معلومات درج نہ کریں۔ CLICKWISE جمع کرایا گیا پیغام محفوظ نہیں رکھتا۔"
        else "Your submitted content is sent to Google's Gemini service via Firebase AI Logic for AI analysis. Do not submit passwords, OTPs, banking credentials or other private information. CLICKWISE does not intentionally retain submitted message content."

    fun emptyInputError(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "تجزیہ شروع کرنے کے لیے براہِ کرم کوئی پیغام یا لنک درج کریں۔"
        else "Please enter a message or suspicious URL before running analysis."

    // Results
    fun analysisComplete(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "تجزیہ مکمل" else "ANALYSIS COMPLETE"

    fun heuristicNote(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "نوٹ: یہ ایک تخمینی اسکور ہے، دھوکہ دہی کا حتمی فیصد یا قانونی ضمانت نہیں۔ علامات نہ ملنے کا مطلب پیغام کا مصدقہ ہونا نہیں ہے۔"
        else "Estimated heuristic risk indicator — not a probability percentage. Absence of warning signs does not prove a message is legitimate."

    fun riskLabel(level: RiskLevel, lang: AppLanguage): String = when (level) {
        RiskLevel.LOW -> if (lang == AppLanguage.UR) "کم خطرہ (LOW)" else "LOW RISK"
        RiskLevel.MEDIUM -> if (lang == AppLanguage.UR) "درمیانہ خطرہ (MEDIUM)" else "MEDIUM RISK"
        RiskLevel.HIGH -> if (lang == AppLanguage.UR) "اعلیٰ خطرہ (HIGH)" else "HIGH RISK"
        RiskLevel.CRITICAL -> if (lang == AppLanguage.UR) "انتہائی خطرہ (CRITICAL)" else "CRITICAL RISK"
    }

    // URL Inspector
    fun urlInspectorHeadline(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "ہر لنک کی ایک کہانی ہوتی ہے۔ اپنا لنک جانچیں۔"
        else "Every link has a story. Inspect yours."

    fun urlInspectorDisclaimer(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "صرف ساختی پیٹرن کا تجزیہ۔ یہ جانچ کسی ویب سائٹ کی اصل ساکھ یا محفوظ ہونے کی تصدیق نہیں کرتی۔"
        else "Pattern analysis only. This scan does not establish the website's actual reputation or safety."

    fun inspectUrlButton(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "لنک کا معائنہ کریں" else "INSPECT URL"

    // Academy & Quiz
    fun academyHeadline(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "خطرے کو جانیں۔ محفوظ کلک کریں۔"
        else "Know the threat. Own the click."

    fun quizHeadline(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "کیا آپ خطرہ پہچان سکتے ہیں؟"
        else "Can You Spot the Threat?"

    // History
    fun historyHeadline(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "حالیہ تجزیے — صرف موجودہ سیشن"
        else "RECENT ANALYSES — THIS SESSION"

    // About Disclaimer
    fun responsibleAiDisclaimer(lang: AppLanguage) =
        if (lang == AppLanguage.UR) "CLICKWISE تعلیمی اور معاون حفاظتی جائزے فراہم کرتا ہے۔ اس کا تجزیہ کسی سائبر حملے سے تحفظ کی ضمانت، فرانزک تفتیش یا پیشہ ورانہ سیکیورٹی خدمات کا متبادل نہیں ہے۔ اہم پیغامات اور ویب سائٹس کی ہمیشہ آزادانہ تصدیق کریں۔"
        else "CLICKWISE provides educational, AI-assisted risk assessments. Its analysis is not a guarantee, a forensic investigation or a substitute for professional cybersecurity services. Always independently verify important messages and websites."
}
