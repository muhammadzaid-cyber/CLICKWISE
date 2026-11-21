package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.domain.model.AppLanguage
import com.example.domain.model.UrlInspectionResult
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.RiskScoreGauge
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.SeverityPill
import com.example.ui.components.TechnicalPanel
import com.example.ui.components.UrlAnatomyDiagram
import com.example.ui.theme.LocalClickwiseColors
import com.example.ui.viewmodel.UrlInspectorScreenState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UrlInspectorScreen(
    state: UrlInspectorScreenState,
    language: AppLanguage,
    reduceMotion: Boolean,
    onUrlChanged: (String) -> Unit,
    onClear: () -> Unit,
    onInspect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val clipboard = LocalClipboardManager.current

    val sampleUrls = listOf(
        "http://hec-scholarship-claim.xyz/portal-auth/verify",
        "http://198.51.100.42/redelivery/confirm",
        "http://portal@reward-claim-center.click/claim",
        "https://bit.ly/3xFakeInternOffer"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("url_inspector_list"),
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
                SectionEyebrow(
                    text = if (language == AppLanguage.UR) "مقامی لنک معائنہ کار" else "LOCAL URL PATTERN INSPECTOR",
                    accentColor = colors.limeSignal
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ClickwiseStrings.urlInspectorHeadline(language),
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = ClickwiseStrings.urlInspectorDisclaimer(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.warnAmber
                )
            }
        }

        // URL Input Workbench
        item {
            TechnicalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp),
                borderColor = colors.borderCyan,
                showCornerReticles = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.UR) "جانچ کے لیے لنک درج کریں" else "PASTE URL TO INSPECT LOCALLY",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Row {
                        TextButton(
                            onClick = {
                                val pasted = clipboard.getText()?.text.orEmpty()
                                if (pasted.isNotBlank()) onUrlChanged(pasted)
                            },
                            modifier = Modifier.testTag("url_paste_button")
                        ) {
                            Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(ClickwiseStrings.pasteButton(language), style = MaterialTheme.typography.labelMedium)
                        }
                        if (state.urlInput.isNotEmpty()) {
                            TextButton(
                                onClick = onClear,
                                modifier = Modifier.testTag("url_clear_button")
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(ClickwiseStrings.clearButton(language), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = state.urlInput,
                    onValueChange = onUrlChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("url_inspector_input"),
                    placeholder = {
                        Text("https://example.com/verify", color = colors.textSecondary.copy(alpha = 0.6f))
                    },
                    leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null, tint = colors.cyanPrimary) },
                    isError = state.validationError != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyanPrimary,
                        unfocusedBorderColor = colors.borderMuted,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.bgPrimary.copy(alpha = 0.78f),
                        unfocusedContainerColor = colors.bgPrimary.copy(alpha = 0.60f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                if (state.validationError != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.alertRed
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (language == AppLanguage.UR) "فرضی امتحانی لنکس:" else "FICTIONAL TEST URLS:",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleUrls.forEachIndexed { idx, url ->
                        FilterChip(
                            selected = state.urlInput == url,
                            onClick = { onInspect(url) },
                            modifier = Modifier.testTag("url_sample_$idx"),
                            label = {
                                Text(
                                    text = url.removePrefix("http://").removePrefix("https://").take(28),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.surfaceDeep,
                                labelColor = colors.textSecondary,
                                selectedContainerColor = colors.cyanPrimary.copy(alpha = 0.16f),
                                selectedLabelColor = colors.cyanPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onInspect(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("url_inspect_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cyanPrimary,
                        contentColor = colors.bgPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = ClickwiseStrings.inspectUrlButton(language),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        val inspection = state.result
        if (inspection != null) {
            // Verdict + Live Anatomy Trace Panel
            item {
                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1040.dp),
                    borderColor = colors.borderCyan,
                    containerColor = colors.bgSecondary
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 600.dp
                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                RiskScoreGauge(
                                    score = inspection.patternRiskScore,
                                    riskLevel = inspection.patternRiskLevel,
                                    language = language,
                                    reduceMotion = reduceMotion,
                                    size = 152.dp
                                )
                                InspectionSummaryBlock(
                                    inspection = inspection,
                                    language = language,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                RiskScoreGauge(
                                    score = inspection.patternRiskScore,
                                    riskLevel = inspection.patternRiskLevel,
                                    language = language,
                                    reduceMotion = reduceMotion,
                                    size = 148.dp
                                )
                                Spacer(Modifier.height(14.dp))
                                InspectionSummaryBlock(
                                    inspection = inspection,
                                    language = language,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    UrlAnatomyDiagram(
                        scheme = inspection.scheme,
                        host = inspection.host,
                        subdomains = inspection.subdomains,
                        pathAndQuery = inspection.pathAndQuery,
                        language = language,
                        hasWarnings = inspection.riskIndicators.isNotEmpty()
                    )
                }
            }

            // Asymmetric 2-Column Breakdown on Wide Screens (Facts | Suspected Risks)
            item {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1040.dp)
                ) {
                    val isWide = maxWidth >= 740.dp
                    if (isWide) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ObservedComponentsPanel(
                                inspection = inspection,
                                language = language,
                                modifier = Modifier
                                    .weight(0.95f)
                                    .fillMaxHeight()
                            )
                            SuspectedRisksPanel(
                                inspection = inspection,
                                language = language,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ObservedComponentsPanel(
                                inspection = inspection,
                                language = language,
                                modifier = Modifier.fillMaxWidth()
                            )
                            SuspectedRisksPanel(
                                inspection = inspection,
                                language = language,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun InspectionSummaryBlock(
    inspection: UrlInspectionResult,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RiskLevelBadge(level = inspection.patternRiskLevel, language = language)
        Text(
            text = if (language == AppLanguage.UR) "مقامی ساختی جائزہ" else "PATTERN RISK ASSESSMENT",
            style = MaterialTheme.typography.labelMedium,
            color = colors.cyanPrimary
        )
        Text(
            text = if (inspection.riskIndicators.isEmpty()) {
                if (language == AppLanguage.UR)
                    "کوئی عام مشکوک پیٹرن نہیں ملا۔ تاہم یہ لنک کے محفوظ ہونے کی تصدیق نہیں کرتا۔"
                else
                    "No common suspicious structural patterns detected locally. Still, this does not establish the website is safe or authentic."
            } else {
                if (language == AppLanguage.UR)
                    "${inspection.riskIndicators.size} مشکوک پیٹرن علامات پائی گئیں۔ براہِ کرم اس لنک کو نہ کھولیں۔"
                else
                    "${inspection.riskIndicators.size} structural risk indicator(s) observed locally. Do not open unverified links."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun ObservedComponentsPanel(
    inspection: UrlInspectionResult,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        accentRailColor = colors.cyanPrimary
    ) {
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "مشاہدہ کردہ ساختی اجزاء (حقائق)" else "OBSERVED URL COMPONENTS (FACTS)"
        )
        Spacer(Modifier.height(10.dp))
        inspection.observations.forEachIndexed { idx, obs ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (language == AppLanguage.UR) obs.labelUr else obs.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = obs.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.cyanPrimary
                )
            }
            if (idx < inspection.observations.lastIndex) {
                HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.35f))
            }
        }
    }
}

@Composable
private fun SuspectedRisksPanel(
    inspection: UrlInspectionResult,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        accentRailColor = if (inspection.riskIndicators.isEmpty()) colors.limeSignal else colors.warnAmber
    ) {
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "ممکنہ پیٹرن خطرات" else "SUSPECTED PATTERN RISKS",
            accentColor = colors.warnAmber
        )
        Spacer(Modifier.height(10.dp))
        if (inspection.riskIndicators.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = colors.limeSignal)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.UR)
                        "کوئی مقامی ساختی خطرہ شناخت نہیں ہوا۔ ہمیشہ HTTPS کے باوجود ڈومین کی تصدیق خود کریں۔"
                    else
                        "No suspicious local URL patterns matched. Remember: HTTPS encryption alone never proves a domain is trustworthy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        } else {
            inspection.riskIndicators.forEach { ind ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceDeep,
                    border = BorderStroke(1.dp, colors.borderMuted)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.UR) ind.titleUr else ind.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            SeverityPill(severity = ind.severity)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = ind.observation,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.cyanPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.UR) ind.explanationUr else ind.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.bgPrimary.copy(alpha = 0.68f),
            border = BorderStroke(1.dp, colors.borderMuted)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ClickwiseStrings.urlInspectorDisclaimer(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}
