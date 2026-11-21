package com.example.domain.model

import androidx.compose.runtime.Immutable

enum class AppLanguage(val code: String, val displayLabel: String) {
    EN("en", "English"),
    UR("ur", "اردو")
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score >= 75 -> CRITICAL
            score >= 50 -> HIGH
            score >= 25 -> MEDIUM
            else -> LOW
        }
    }
}

enum class FindingSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class ActionPriority {
    IMMEDIATE,
    RECOMMENDED,
    VERIFY
}

@Immutable
data class AnalysisFinding(
    val title: String,
    val titleUr: String,
    val severity: FindingSeverity,
    val evidence: String,
    val explanation: String,
    val explanationUr: String,
    val detailedExplanation: String,
    val detailedExplanationUr: String
)

@Immutable
data class RecommendedAction(
    val priority: ActionPriority,
    val title: String,
    val titleUr: String,
    val description: String,
    val descriptionUr: String
)

@Immutable
data class UrlObservation(
    val label: String,
    val labelUr: String,
    val value: String
)

@Immutable
data class UrlRiskIndicator(
    val title: String,
    val titleUr: String,
    val severity: FindingSeverity,
    val observation: String,
    val explanation: String,
    val explanationUr: String
)

@Immutable
data class UrlInspectionResult(
    val rawUrl: String,
    val isValidFormat: Boolean,
    val scheme: String,
    val host: String,
    val port: String,
    val subdomains: String,
    val pathAndQuery: String,
    val observations: List<UrlObservation>,
    val riskIndicators: List<UrlRiskIndicator>,
    val patternRiskScore: Int,
    val patternRiskLevel: RiskLevel
)

data class MessageAnalysisRequest(
    val message: String,
    val optionalUrl: String = "",
    val language: AppLanguage = AppLanguage.EN
)

@Immutable
data class MessageAnalysisResult(
    val id: String,
    val shortLabel: String,
    val shortLabelUr: String,
    val analysisType: AnalysisType,
    val timestampEpochMs: Long,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val summary: String,
    val urduSummary: String,
    val suspiciousPhrases: List<String>,
    val findings: List<AnalysisFinding>,
    val redFlags: List<String>,
    val redFlagsUr: List<String>,
    val positiveSignals: List<String>,
    val positiveSignalsUr: List<String>,
    val recommendedActions: List<RecommendedAction>,
    val uncertainties: List<String>,
    val uncertaintiesUr: List<String>,
    val urlInspection: UrlInspectionResult? = null,
    val isPlaceholderMode: Boolean = true
)

enum class AnalysisType {
    MESSAGE_ONLY,
    MESSAGE_AND_URL,
    URL_ONLY
}

@Immutable
data class SessionScanRecord(
    val id: String,
    val shortLabel: String,
    val shortLabelUr: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val timestampEpochMs: Long,
    val analysisType: AnalysisType,
    val result: MessageAnalysisResult
)

@Immutable
data class SampleScenario(
    val id: String,
    val category: String,
    val categoryUr: String,
    val title: String,
    val titleUr: String,
    val message: String,
    val messageUr: String,
    val sampleUrl: String
)

@Immutable
data class LessonQuestion(
    val prompt: String,
    val promptUr: String,
    val options: List<String>,
    val optionsUr: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val explanationUr: String
)

@Immutable
data class CyberLesson(
    val id: String,
    val number: Int,
    val title: String,
    val titleUr: String,
    val intro: String,
    val introUr: String,
    val explanation: String,
    val explanationUr: String,
    val illustrativeExample: String,
    val illustrativeExampleUr: String,
    val warningSigns: List<String>,
    val warningSignsUr: List<String>,
    val protectionTips: List<String>,
    val protectionTipsUr: List<String>,
    val knowledgeCheck: LessonQuestion
)

@Immutable
data class QuizQuestion(
    val id: String,
    val topic: String,
    val topicUr: String,
    val question: String,
    val questionUr: String,
    val options: List<String>,
    val optionsUr: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val explanationUr: String
)
