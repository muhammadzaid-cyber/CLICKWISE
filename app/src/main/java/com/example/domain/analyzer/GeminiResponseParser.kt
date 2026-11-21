package com.example.domain.analyzer

import com.example.domain.model.ActionPriority
import com.example.domain.model.AnalysisFinding
import com.example.domain.model.AnalysisType
import com.example.domain.model.FindingSeverity
import com.example.domain.model.MessageAnalysisResult
import com.example.domain.model.RecommendedAction
import com.example.domain.model.RiskLevel
import com.example.domain.model.UrlInspectionResult
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

object GeminiResponseParser {

    fun parse(
        jsonString: String,
        analysisType: AnalysisType,
        urlInspection: UrlInspectionResult?,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): MessageAnalysisResult {
        val root = JSONObject(jsonString)
        if (!root.has("riskScore") || root.isNull("riskScore")) {
            throw IllegalArgumentException("Model response is missing required field 'riskScore'.")
        }
        val rawScore = root.getInt("riskScore").coerceIn(0, 100)
        val summary = root.optString("summary").trim()
        if (summary.isBlank()) {
            throw IllegalArgumentException("Model response is missing required field 'summary'.")
        }
        val parsedRiskLevel = RiskLevel.fromScore(rawScore)

        val shortLabel = root.optString("shortLabel").ifBlank { "Security Assessment" }
        val shortLabelUr = root.optString("shortLabelUr").ifBlank { "سیکیورٹی جائزہ" }
        val urduSummary = root.optString("urduSummary").ifBlank { summary }

        val suspiciousPhrases = root.optJSONArray("suspiciousPhrases").toStringList()
        val findings = parseFindings(root.optJSONArray("findings")).toMutableList()
        val redFlags = root.optJSONArray("redFlags").toStringList()
        val redFlagsUr = root.optJSONArray("redFlagsUr").toStringList()
        val positiveSignals = root.optJSONArray("positiveSignals").toStringList()
        val positiveSignalsUr = root.optJSONArray("positiveSignalsUr").toStringList()
        val recommendedActions = parseActions(root.optJSONArray("recommendedActions")).ifEmpty {
            listOf(
                RecommendedAction(
                    priority = ActionPriority.VERIFY,
                    title = "Verify independently through official channels",
                    titleUr = "سرکاری ذرائع سے آزادانہ تصدیق کریں",
                    description = "Always confirm unexpected announcements via the organization's official website or helpline.",
                    descriptionUr = "غیر متوقع اعلانات کی تصدیق ہمیشہ متعلقہ ادارے کی اصل ویب سائٹ یا ہیلپ لائن سے کریں۔"
                )
            )
        }
        val uncertainties = root.optJSONArray("uncertainties").toStringList().ifEmpty {
            listOf("The sender's real-world identity could not be independently verified.")
        }
        val uncertaintiesUr = root.optJSONArray("uncertaintiesUr").toStringList().ifEmpty {
            listOf("بھیجنے والے کی اصل شناخت کی آزادانہ تصدیق ممکن نہیں۔")
        }

        // Append deterministic local URL structural findings cleanly if present
        if (urlInspection != null && urlInspection.riskIndicators.isNotEmpty()) {
            urlInspection.riskIndicators.forEach { indicator ->
                findings += AnalysisFinding(
                    title = "Local URL Check: ${indicator.title}",
                    titleUr = "مقامی لنک معائنہ: ${indicator.titleUr}",
                    severity = indicator.severity,
                    evidence = indicator.observation,
                    explanation = indicator.explanation,
                    explanationUr = indicator.explanationUr,
                    detailedExplanation = "Local structural inspection observed: ${indicator.observation}. ${indicator.explanation}",
                    detailedExplanationUr = "مقامی ساختی معائنے میں مشاہدہ: ${indicator.observation}۔ ${indicator.explanationUr}"
                )
            }
        }

        return MessageAnalysisResult(
            id = UUID.randomUUID().toString(),
            shortLabel = shortLabel,
            shortLabelUr = shortLabelUr,
            analysisType = analysisType,
            timestampEpochMs = timestampEpochMs,
            riskScore = rawScore,
            riskLevel = parsedRiskLevel,
            summary = summary,
            urduSummary = urduSummary,
            suspiciousPhrases = suspiciousPhrases,
            findings = findings,
            redFlags = redFlags,
            redFlagsUr = redFlagsUr,
            positiveSignals = positiveSignals,
            positiveSignalsUr = positiveSignalsUr,
            recommendedActions = recommendedActions,
            uncertainties = uncertainties,
            uncertaintiesUr = uncertaintiesUr,
            urlInspection = urlInspection,
            isPlaceholderMode = false
        )
    }

    private fun parseRiskLevel(raw: String, fallbackScore: Int): RiskLevel {
        return when (raw.uppercase(Locale.US).trim()) {
            "LOW" -> RiskLevel.LOW
            "MEDIUM" -> RiskLevel.MEDIUM
            "HIGH" -> RiskLevel.HIGH
            "CRITICAL" -> RiskLevel.CRITICAL
            else -> RiskLevel.fromScore(fallbackScore)
        }
    }

    private fun parseFindings(array: JSONArray?): List<AnalysisFinding> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val severity = when (obj.optString("severity").uppercase(Locale.US).trim()) {
                    "CRITICAL" -> FindingSeverity.CRITICAL
                    "HIGH" -> FindingSeverity.HIGH
                    "MEDIUM" -> FindingSeverity.MEDIUM
                    else -> FindingSeverity.LOW
                }
                val title = obj.optString("title").ifBlank { "Security Indicator" }
                val explanation = obj.optString("explanation").ifBlank { "" }
                add(
                    AnalysisFinding(
                        title = title,
                        titleUr = obj.optString("titleUr").ifBlank { title },
                        severity = severity,
                        evidence = obj.optString("evidence").ifBlank { "Observed in submitted text" },
                        explanation = explanation,
                        explanationUr = obj.optString("explanationUr").ifBlank { explanation },
                        detailedExplanation = obj.optString("detailedExplanation").ifBlank { explanation },
                        detailedExplanationUr = obj.optString("detailedExplanationUr")
                            .ifBlank { obj.optString("explanationUr").ifBlank { explanation } }
                    )
                )
            }
        }
    }

    private fun parseActions(array: JSONArray?): List<RecommendedAction> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val priority = when (obj.optString("priority").uppercase(Locale.US).trim()) {
                    "IMMEDIATE" -> ActionPriority.IMMEDIATE
                    "VERIFY" -> ActionPriority.VERIFY
                    else -> ActionPriority.RECOMMENDED
                }
                val title = obj.optString("title").ifBlank { "Verify independently" }
                val desc = obj.optString("description").ifBlank { "" }
                add(
                    RecommendedAction(
                        priority = priority,
                        title = title,
                        titleUr = obj.optString("titleUr").ifBlank { title },
                        description = desc,
                        descriptionUr = obj.optString("descriptionUr").ifBlank { desc }
                    )
                )
            }
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val item = optString(i).trim()
                if (item.isNotEmpty()) add(item)
            }
        }
    }
}
