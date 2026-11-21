package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.domain.model.ActionPriority
import com.example.domain.model.AnalysisFinding
import com.example.domain.model.AnalysisType
import com.example.domain.model.AppLanguage
import com.example.domain.model.FindingSeverity
import com.example.domain.model.MessageAnalysisResult
import com.example.domain.model.RiskLevel
import com.example.ui.components.PlaceholderAiNoticeBanner
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.RiskScoreGauge
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.SeverityPill
import com.example.ui.components.TechnicalPanel
import com.example.ui.components.UrlAnatomyDiagram
import com.example.ui.theme.LocalClickwiseColors
import com.example.utils.ReportPrinter
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalysisResultView(
    result: MessageAnalysisResult,
    language: AppLanguage,
    reduceMotion: Boolean,
    onAnalyzeAnother: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenLesson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onAnalyzeAnother()
    }

    val colors = LocalClickwiseColors.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var copiedFeedback by remember { mutableStateOf(false) }

    val formattedReport = remember(result, language) {
        buildPlainTextReport(result, language)
    }

    val verdictBorder = when (result.riskLevel) {
        RiskLevel.LOW -> colors.limeSignal.copy(alpha = 0.45f)
        RiskLevel.MEDIUM, RiskLevel.HIGH -> colors.warnAmber.copy(alpha = 0.55f)
        RiskLevel.CRITICAL -> colors.alertRed.copy(alpha = 0.65f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("result_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1040.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionEyebrow(text = ClickwiseStrings.analysisComplete(language))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.surfaceDeep,
                            border = BorderStroke(1.dp, colors.borderMuted)
                        ) {
                            Text(
                                text = result.analysisType.name.replace('_', ' '),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.UR)
                            "آپ کا سیکیورٹی جائزہ تیار ہے۔"
                        else
                            "Your security assessment is ready.",
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.textPrimary
                    )
                }
            }

            if (result.isPlaceholderMode) {
                item {
                    PlaceholderAiNoticeBanner(
                        language = language,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1040.dp)
                    )
                }
            }

            // Executive Verdict Header (Gauge + Summary + Heuristic Note)
            item {
                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1040.dp),
                    borderColor = verdictBorder,
                    containerColor = colors.bgSecondary,
                    cornerRadius = 18.dp,
                    showCornerReticles = true
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 600.dp
                        if (isWide) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(28.dp)
                            ) {
                                RiskScoreGauge(
                                    score = result.riskScore,
                                    riskLevel = result.riskLevel,
                                    language = language,
                                    reduceMotion = reduceMotion,
                                    size = 184.dp
                                )
                                OverallSummaryColumn(
                                    result = result,
                                    language = language,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                RiskScoreGauge(
                                    score = result.riskScore,
                                    riskLevel = result.riskLevel,
                                    language = language,
                                    reduceMotion = reduceMotion,
                                    size = 166.dp
                                )
                                Spacer(Modifier.height(16.dp))
                                OverallSummaryColumn(
                                    result = result,
                                    language = language,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Local URL Structural Assessment with Visual UrlAnatomyDiagram
            val urlInspection = result.urlInspection
            if (urlInspection != null) {
                item {
                    TechnicalPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1040.dp),
                        borderColor = colors.limeSignal.copy(alpha = 0.35f),
                        accentRailColor = colors.limeSignal
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Link,
                                contentDescription = null,
                                tint = colors.limeSignal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.UR)
                                    "مقامی لنک ساختی معائنہ"
                                else
                                    "LOCAL URL PATTERN ASSESSMENT",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.limeSignal
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        UrlAnatomyDiagram(
                            scheme = urlInspection.scheme,
                            host = urlInspection.host,
                            subdomains = urlInspection.subdomains,
                            pathAndQuery = urlInspection.pathAndQuery,
                            language = language,
                            hasWarnings = urlInspection.riskIndicators.isNotEmpty()
                        )
                        Spacer(Modifier.height(12.dp))
                        urlInspection.observations.forEachIndexed { idx, obs ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (language == AppLanguage.UR) obs.labelUr else obs.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = obs.value,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.cyanPrimary
                                )
                            }
                            if (idx < urlInspection.observations.lastIndex) {
                                HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.4f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = ClickwiseStrings.urlInspectorDisclaimer(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Suspicious Phrases
            if (result.suspiciousPhrases.isNotEmpty()) {
                item {
                    TechnicalPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1040.dp),
                        accentRailColor = colors.warnAmber
                    ) {
                        SectionEyebrow(
                            text = if (language == AppLanguage.UR) "شناخت شدہ مشکوک الفاظ" else "SUSPICIOUS PHRASES DETECTED",
                            accentColor = colors.warnAmber
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            result.suspiciousPhrases.forEach { phrase ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.warnAmber.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, colors.warnAmber.copy(alpha = 0.42f))
                                ) {
                                    Text(
                                        text = "\"$phrase\"",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.warnAmber,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Evidence Findings (Expandable)
            if (result.findings.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1040.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionEyebrow(
                            text = if (language == AppLanguage.UR) "شواہد اور تفصیلی علامات" else "EVIDENCE & WARNING INDICATORS",
                            accentColor = colors.alertRed
                        )
                        result.findings.forEachIndexed { idx, finding ->
                            ExpandableFindingRow(
                                finding = finding,
                                language = language,
                                reduceMotion = reduceMotion,
                                index = idx
                            )
                        }
                    }
                }
            }

            // Positive Indicators & Remaining Uncertainties Split on Wide Screens
            val positiveList = if (language == AppLanguage.UR) result.positiveSignalsUr else result.positiveSignals
            val uncertaintiesList = if (language == AppLanguage.UR) result.uncertaintiesUr else result.uncertainties
            item {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1040.dp)
                ) {
                    val isWide = maxWidth >= 680.dp && positiveList.isNotEmpty()
                    if (isWide) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PositiveSignalsPanel(
                                positiveList = positiveList,
                                language = language,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            UncertaintiesPanel(
                                uncertaintiesList = uncertaintiesList,
                                language = language,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (positiveList.isNotEmpty()) {
                                PositiveSignalsPanel(
                                    positiveList = positiveList,
                                    language = language,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            UncertaintiesPanel(
                                uncertaintiesList = uncertaintiesList,
                                language = language,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Recommended Safety Actions
            item {
                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1040.dp),
                    borderColor = colors.borderCyan
                ) {
                    SectionEyebrow(
                        text = if (language == AppLanguage.UR) "تجویز کردہ حفاظتی اقدامات" else "RECOMMENDED SAFETY ACTIONS"
                    )
                    Spacer(Modifier.height(12.dp))
                    result.recommendedActions.forEach { action ->
                        val priorityColor = when (action.priority) {
                            ActionPriority.IMMEDIATE -> colors.alertRed
                            ActionPriority.RECOMMENDED -> colors.cyanPrimary
                            ActionPriority.VERIFY -> colors.limeSignal
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surfaceDeep,
                            border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.28f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = priorityColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = action.priority.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = priorityColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.UR) action.titleUr else action.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = if (language == AppLanguage.UR) action.descriptionUr else action.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Result Actions Toolbar
            item {
                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1040.dp),
                    containerColor = colors.bgSecondary
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(formattedReport))
                                copiedFeedback = true
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (language == AppLanguage.UR) "رپورٹ کلپ بورڈ پر کاپی ہو گئی" else "Security report copied to clipboard"
                                    )
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("result_copy_report_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cyanPrimary,
                                contentColor = colors.bgPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (copiedFeedback) {
                                    if (language == AppLanguage.UR) "کاپی ہو گئی" else "COPIED"
                                } else {
                                    if (language == AppLanguage.UR) "رپورٹ کاپی کریں" else "COPY REPORT"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val started = ReportPrinter.printReport(
                                    context = context,
                                    jobName = "CLICKWISE_Security_Report",
                                    reportText = formattedReport
                                )
                                if (!started) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (language == AppLanguage.UR) "پرنٹ سروس دستیاب نہیں ہے" else "Print service is unavailable on this device"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("result_print_report_button"),
                            border = BorderStroke(1.dp, colors.borderCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.UR) "رپورٹ پرنٹ / پی ڈی ایف" else "PRINT / SAVE PDF",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CLICKWISE Security Report")
                                    putExtra(Intent.EXTRA_TEXT, formattedReport)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Security Report"))
                                } catch (_: ActivityNotFoundException) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (language == AppLanguage.UR) "شیئرنگ سروس دستیاب نہیں ہے" else "No share handler available on this device"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("result_share_report_button"),
                            border = BorderStroke(1.dp, colors.borderMuted),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.UR) "شیئر کریں" else "SHARE REPORT",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = { onOpenLesson(resolveRelevantLessonId(result)) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("result_view_lesson_button"),
                            border = BorderStroke(1.dp, colors.borderMuted),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.UR) "سیکیورٹی سبق دیکھیں" else "VIEW SECURITY LESSON",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = onToggleLanguage,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("result_switch_language_button"),
                            border = BorderStroke(1.dp, colors.borderMuted),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.EN) "اردو میں دیکھیں" else "Switch to English",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.cyanPrimary
                            )
                        }

                        Button(
                            onClick = onAnalyzeAnother,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("result_analyze_another_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceDeep,
                                contentColor = colors.cyanPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.UR) "دوسرا پیغام جانچیں" else "ANALYSE ANOTHER MESSAGE",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun OverallSummaryColumn(
    result: MessageAnalysisResult,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RiskLevelBadge(level = result.riskLevel, language = language)
            Text(
                text = if (language == AppLanguage.UR) result.shortLabelUr else result.shortLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
        }
        Text(
            text = if (language == AppLanguage.UR) result.urduSummary else result.summary,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.bgPrimary.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, colors.borderMuted)
        ) {
            Text(
                text = ClickwiseStrings.heuristicNote(language),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun PositiveSignalsPanel(
    positiveList: List<String>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        borderColor = colors.limeSignal.copy(alpha = 0.34f),
        accentRailColor = colors.limeSignal
    ) {
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "مثبت مشاہدات" else "POSITIVE INDICATORS",
            accentColor = colors.limeSignal
        )
        Spacer(Modifier.height(10.dp))
        positiveList.forEach { signal ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = colors.limeSignal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = signal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun UncertaintiesPanel(
    uncertaintiesList: List<String>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        containerColor = colors.surfaceDeep
    ) {
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "باقی غیریقینی پہلو" else "REMAINING UNCERTAINTIES",
            accentColor = colors.textSecondary
        )
        Spacer(Modifier.height(10.dp))
        uncertaintiesList.forEach { item ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ExpandableFindingRow(
    finding: AnalysisFinding,
    language: AppLanguage,
    reduceMotion: Boolean,
    index: Int
) {
    val colors = LocalClickwiseColors.current
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 220),
        label = "findingChevron"
    )
    val railColor = when (finding.severity) {
        FindingSeverity.LOW -> colors.limeSignal
        FindingSeverity.MEDIUM, FindingSeverity.HIGH -> colors.warnAmber
        FindingSeverity.CRITICAL -> colors.alertRed
    }

    TechnicalPanel(
        modifier = Modifier
            .clickable { expanded = !expanded }
            .testTag("result_finding_$index"),
        containerColor = colors.surfaceDeep,
        borderColor = railColor.copy(alpha = 0.35f),
        accentRailColor = railColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = railColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.UR) finding.titleUr else finding.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            }
            Spacer(Modifier.width(8.dp))
            SeverityPill(severity = finding.severity)
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse details" else "Expand details",
                tint = colors.textSecondary,
                modifier = Modifier.rotate(chevronRotation)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = finding.evidence,
            style = MaterialTheme.typography.labelMedium,
            color = colors.cyanPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (language == AppLanguage.UR) finding.explanationUr else finding.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.bgPrimary.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, colors.borderMuted)
                ) {
                    Text(
                        text = if (language == AppLanguage.UR) finding.detailedExplanationUr else finding.detailedExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

private fun buildPlainTextReport(result: MessageAnalysisResult, lang: AppLanguage): String {
    return buildString {
        appendLine("CLICKWISE — SECURITY ASSESSMENT REPORT")
        appendLine("THINK BEFORE YOU CLICK.")
        appendLine("----------------------------------------")
        appendLine("Risk Score (Heuristic): ${result.riskScore} / 100")
        appendLine("Classification: ${ClickwiseStrings.riskLabel(result.riskLevel, lang)}")
        appendLine(
            if (result.isPlaceholderMode) "Mode: Local Heuristic & Placeholder Assessment"
            else "Mode: Gemini Developer API via Firebase AI Logic"
        )
        appendLine()
        appendLine("Summary:")
        appendLine(if (lang == AppLanguage.UR) result.urduSummary else result.summary)
        appendLine()
        if (result.urlInspection != null) {
            appendLine("Local URL Pattern Inspection:")
            result.urlInspection.observations.forEach { obs ->
                appendLine("- ${if (lang == AppLanguage.UR) obs.labelUr else obs.label}: ${obs.value}")
            }
            appendLine()
        }
        if (result.findings.isNotEmpty()) {
            appendLine("Findings:")
            result.findings.forEach { f ->
                appendLine("- [${f.severity}] ${if (lang == AppLanguage.UR) f.titleUr else f.title}")
                appendLine("  Evidence: ${f.evidence}")
                appendLine("  ${if (lang == AppLanguage.UR) f.explanationUr else f.explanation}")
            }
            appendLine()
        }
        appendLine("Recommended Actions:")
        result.recommendedActions.forEach { a ->
            appendLine("- (${a.priority}) ${if (lang == AppLanguage.UR) a.titleUr else a.title}: ${if (lang == AppLanguage.UR) a.descriptionUr else a.description}")
        }
        appendLine()
        appendLine(ClickwiseStrings.responsibleAiDisclaimer(lang))
    }
}

private fun resolveRelevantLessonId(result: MessageAnalysisResult): String {
    val text = buildString {
        append(result.shortLabel).append(' ')
        append(result.summary).append(' ')
        result.findings.forEach { append(it.title).append(' ') }
    }.lowercase()
    return when {
        "scholarship" in text || "stipend" in text -> "lesson_scholarships"
        "job" in text || "internship" in text || "recruit" in text -> "lesson_jobs"
        "otp" in text || "pin" in text -> "lesson_otp"
        result.analysisType == AnalysisType.URL_ONLY ||
            (result.urlInspection?.riskIndicators?.isNotEmpty() == true && "url" in text) -> "lesson_urls"
        else -> "lesson_phishing"
    }
}
