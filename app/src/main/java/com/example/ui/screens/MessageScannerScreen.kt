package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.data.EducationalContentRepository
import com.example.domain.analyzer.AnalysisStage
import com.example.domain.model.AppLanguage
import com.example.domain.model.SampleScenario
import com.example.ui.components.AnimatedPulseBeacon
import com.example.ui.components.CyberRadarVisualizer
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.TechnicalPanel
import com.example.ui.theme.LocalClickwiseColors
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.MAX_MESSAGE_LENGTH
import com.example.ui.viewmodel.ScannerScreenState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageScannerScreen(
    state: ScannerScreenState,
    language: AppLanguage,
    reduceMotion: Boolean,
    onMessageChanged: (String) -> Unit,
    onOptionalUrlChanged: (String) -> Unit,
    onClearInputs: () -> Unit,
    onSelectSample: (SampleScenario) -> Unit,
    onAnalyze: () -> Unit,
    onResetToNewScan: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenAcademyLesson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val clipboard = LocalClipboardManager.current

    when (val analysis = state.analysisState) {
        is AnalysisUiState.Success -> {
            AnalysisResultView(
                result = analysis.result,
                language = language,
                reduceMotion = reduceMotion,
                onAnalyzeAnother = onResetToNewScan,
                onToggleLanguage = onToggleLanguage,
                onOpenLesson = onOpenAcademyLesson,
                modifier = modifier
            )
            return
        }

        is AnalysisUiState.Scanning -> {
            ScanningActiveView(
                stage = analysis.stage,
                progress = analysis.progress,
                language = language,
                reduceMotion = reduceMotion,
                modifier = modifier
            )
            return
        }

        else -> Unit
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("scanner_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1120.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionEyebrow(
                        text = if (language == AppLanguage.UR) "سیکیورٹی اسکینر ورک اسپیس" else "SECURITY ANALYSIS WORKSPACE"
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.limeSignal.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, colors.limeSignal.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedPulseBeacon(color = colors.limeSignal, reduceMotion = reduceMotion, size = 7.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.UR) "GEMINI + URL انجن تیار" else "GEMINI AI + URL ENGINE READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.limeSignal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = ClickwiseStrings.scannerHeadline(language),
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = ClickwiseStrings.scannerSubtitle(language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary
                )
            }
        }

        if (state.analysisState is AnalysisUiState.Error) {
            item {
                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1120.dp),
                    containerColor = colors.alertRed.copy(alpha = 0.08f),
                    borderColor = colors.alertRed.copy(alpha = 0.55f),
                    accentRailColor = colors.alertRed
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = colors.alertRed)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = state.analysisState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary
                            )
                        }
                        TextButton(onClick = onAnalyze) {
                            Text(
                                text = if (language == AppLanguage.UR) "دوبارہ کوشش" else "RETRY",
                                color = colors.cyanPrimary
                            )
                        }
                    }
                }
            }
        }

        // Responsive 2-column Desktop/Wide-Tablet Workspace (Console Form + Live Inspection Scope Pane)
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1120.dp)
            ) {
                val isWide = maxWidth >= 820.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1.45f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SampleScenarioPanel(
                                selectedSampleId = state.selectedSampleId,
                                language = language,
                                onSelectSample = onSelectSample
                            )
                            ScannerComposerPanel(
                                state = state,
                                language = language,
                                onMessageChanged = onMessageChanged,
                                onOptionalUrlChanged = onOptionalUrlChanged,
                                onClearInputs = onClearInputs,
                                onAnalyze = onAnalyze,
                                onToggleLanguage = onToggleLanguage,
                                onPasteMessage = {
                                    val pasted = clipboard.getText()?.text.orEmpty()
                                    if (pasted.isNotBlank()) onMessageChanged(pasted)
                                }
                            )
                        }
                        InspectionScopeSidePanel(
                            language = language,
                            modifier = Modifier.weight(0.85f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SampleScenarioPanel(
                            selectedSampleId = state.selectedSampleId,
                            language = language,
                            onSelectSample = onSelectSample
                        )
                        ScannerComposerPanel(
                            state = state,
                            language = language,
                            onMessageChanged = onMessageChanged,
                            onOptionalUrlChanged = onOptionalUrlChanged,
                            onClearInputs = onClearInputs,
                            onAnalyze = onAnalyze,
                            onToggleLanguage = onToggleLanguage,
                            onPasteMessage = {
                                val pasted = clipboard.getText()?.text.orEmpty()
                                if (pasted.isNotBlank()) onMessageChanged(pasted)
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SampleScenarioPanel(
    selectedSampleId: String?,
    language: AppLanguage,
    onSelectSample: (SampleScenario) -> Unit
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        containerColor = colors.surfaceDeep,
        borderColor = colors.borderMuted
    ) {
        Text(
            text = if (language == AppLanguage.UR)
                "فرضی تعلیمی امتحانی مثالیں (ٹیسٹ کیس منتخب کریں):"
            else
                "FICTIONAL EDUCATIONAL TEST CASES (SELECT TO TEST):",
            style = MaterialTheme.typography.labelMedium,
            color = colors.cyanPrimary
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EducationalContentRepository.sampleScenarios.forEach { scenario ->
                val selected = selectedSampleId == scenario.id
                FilterChip(
                    selected = selected,
                    onClick = { onSelectSample(scenario) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("scanner_sample_${scenario.id}"),
                    label = {
                        Text(
                            text = if (language == AppLanguage.UR) scenario.titleUr else scenario.title,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.bgSecondary,
                        labelColor = colors.textSecondary,
                        selectedContainerColor = colors.cyanPrimary.copy(alpha = 0.18f),
                        selectedLabelColor = colors.cyanPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = colors.borderMuted,
                        selectedBorderColor = colors.cyanPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun ScannerComposerPanel(
    state: ScannerScreenState,
    language: AppLanguage,
    onMessageChanged: (String) -> Unit,
    onOptionalUrlChanged: (String) -> Unit,
    onClearInputs: () -> Unit,
    onAnalyze: () -> Unit,
    onToggleLanguage: () -> Unit,
    onPasteMessage: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        borderColor = colors.borderCyan,
        showCornerReticles = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.UR) "مشکوک پیغام" else "SUSPICIOUS MESSAGE CONTENT",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = onPasteMessage,
                    modifier = Modifier.testTag("scanner_paste_button")
                ) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(ClickwiseStrings.pasteButton(language), style = MaterialTheme.typography.labelMedium)
                }
                if (state.messageInput.isNotEmpty() || state.optionalUrlInput.isNotEmpty()) {
                    TextButton(
                        onClick = onClearInputs,
                        modifier = Modifier.testTag("scanner_clear_button")
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(ClickwiseStrings.clearButton(language), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = state.messageInput,
            onValueChange = onMessageChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .testTag("scanner_message_input"),
            placeholder = {
                Text(
                    text = ClickwiseStrings.messagePlaceholder(language),
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
            },
            isError = state.validationError != null,
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

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = state.validationError ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = colors.alertRed
            )
            Text(
                text = "${state.messageInput.length} / $MAX_MESSAGE_LENGTH",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.optionalUrlInput,
            onValueChange = onOptionalUrlChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scanner_optional_url_input"),
            label = { Text(ClickwiseStrings.optionalUrlLabel(language)) },
            placeholder = { Text("https://example.com/verify", color = colors.textSecondary.copy(alpha = 0.6f)) },
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

        Spacer(Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.bgPrimary.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, colors.borderMuted)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = colors.cyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ClickwiseStrings.privacyNotice(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAnalyze,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("scanner_analyse_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cyanPrimary,
                    contentColor = colors.bgPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Radar, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ClickwiseStrings.analyseButton(language),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            OutlinedButton(
                onClick = onToggleLanguage,
                modifier = Modifier
                    .height(52.dp)
                    .testTag("scanner_language_toggle"),
                border = BorderStroke(1.dp, colors.borderCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.EN) "EN → اردو" else "اردو → EN",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.cyanPrimary
                )
            }
        }
    }
}

@Composable
private fun InspectionScopeSidePanel(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val checks = if (language == AppLanguage.UR) {
        listOf(
            "جعلی عجلت اور خوف دلانے والے الفاظ",
            "او ٹی پی، پن یا موبائل والٹ فیس کا مطالبہ",
            "غیر رسمی یا نقلی ڈومین اور سب ڈومین ساخت",
            "مختصر لنکس (Shorteners) اور IP ایڈریس ہوسٹ",
            "انگریزی اور اردو میں واضح شواہد و حفاظتی اقدامات"
        )
    } else {
        listOf(
            "Artificial urgency, countdown threats & pressure tactics",
            "OTP, PIN, password, or personal wallet fee requests",
            "Lookalike brand domains, odd TLDs & stacked subdomains",
            "URL shorteners, IP literal hosts & credential '@' tricks",
            "Bilingual evidence breakdown & prioritized safe actions"
        )
    }

    TechnicalPanel(
        modifier = modifier,
        containerColor = colors.surfaceDeep,
        borderColor = colors.borderMuted,
        accentRailColor = colors.indigoAccent
    ) {
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "معائنہ کا دائرہ کار" else "DIAGNOSTIC CHECKLIST",
            accentColor = colors.indigoAccent
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (language == AppLanguage.UR)
                "CLICKWISE ہر پیغام کو مقامی URL پیٹرن اور جیمینائی AI کے ذریعے 5 سطحوں پر پرکھتا ہے:"
            else
                "Every submission passes through local structural parsing and Firebase AI Logic across five signal axes:",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(12.dp))
        checks.forEachIndexed { idx, item ->
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.cyanPrimary.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "0${idx + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.cyanPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = colors.limeSignal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.UR) "خام API کلید APK میں شامل نہیں" else "Zero raw API keys packaged",
                style = MaterialTheme.typography.labelSmall,
                color = colors.limeSignal
            )
        }
    }
}

@Composable
private fun ScanningActiveView(
    stage: AnalysisStage,
    progress: Float,
    language: AppLanguage,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val stages = listOf(
        AnalysisStage.PREPARING to (if (language == AppLanguage.UR) "تیاری اور سینیٹائزیشن" else "Input preparation & sanitization"),
        AnalysisStage.INSPECTING_MESSAGE to (if (language == AppLanguage.UR) "متن اور URL کی ساخت کا معائنہ" else "Message & local URL parsing"),
        AnalysisStage.IDENTIFYING_WARNINGS to (if (language == AppLanguage.UR) "سماجی انجینئرنگ علامات کی تلاش" else "Identifying red flags & urgency lures"),
        AnalysisStage.GENERATING_ASSESSMENT to (if (language == AppLanguage.UR) "جیمینائی AI سیکیورٹی تجزیہ" else "Gemini AI structured assessment"),
        AnalysisStage.FINALIZING to (if (language == AppLanguage.UR) "دوزبانی رپورٹ کی تکمیل" else "Finalizing bilingual report")
    )
    val currentIdx = stages.indexOfFirst { it.first == stage }.coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CyberRadarVisualizer(
            isScanning = true,
            reduceMotion = reduceMotion,
            size = 200.dp
        )
        Spacer(Modifier.height(22.dp))
        TechnicalPanel(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            borderColor = colors.borderCyan,
            showCornerReticles = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionEyebrow(
                    text = if (language == AppLanguage.UR) "سیکیورٹی پائپ لائن فعال" else "DIAGNOSTIC PIPELINE",
                    accentColor = colors.cyanPrimary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.cyanPrimary
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.cyanPrimary,
                trackColor = colors.bgPrimary
            )
            Spacer(Modifier.height(14.dp))
            stages.forEachIndexed { idx, (_, label) ->
                val done = idx < currentIdx
                val active = idx == currentIdx
                val tint = when {
                    done -> colors.limeSignal
                    active -> colors.cyanPrimary
                    else -> colors.textSecondary.copy(alpha = 0.45f)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (done) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (active) {
                        AnimatedPulseBeacon(color = colors.cyanPrimary, reduceMotion = reduceMotion, size = 8.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(tint.copy(alpha = 0.4f))
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = label,
                        style = if (active) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
                        color = tint
                    )
                }
            }
        }
    }
}
