package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.data.UpdatePreferenceSnapshot
import com.example.domain.model.AppLanguage
import com.example.domain.update.UpdateUiState
import com.example.ui.components.AnimatedPulseBeacon
import com.example.ui.components.AppUpdateStatusPanel
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.TechnicalPanel
import com.example.ui.theme.LocalClickwiseColors

@Composable
fun SettingsScreen(
    language: AppLanguage,
    reduceMotion: Boolean,
    sessionHistoryCount: Int,
    installedVersionName: String = "",
    installedVersionCode: Int = 1,
    updateState: UpdateUiState = UpdateUiState.Idle,
    updatePreferences: UpdatePreferenceSnapshot = UpdatePreferenceSnapshot(),
    onSetLanguage: (AppLanguage) -> Unit,
    onSetReduceMotion: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    onOpenUpdateDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_list"),
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
                SectionEyebrow(text = ClickwiseStrings.navSettings(language))
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.UR) "زبان، حرکت اور سیشن کی ترجیحات" else "Language, Accessibility & Session Controls",
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textPrimary
                )
            }
        }

        // Responsive 2-column Control Console on Desktop/Tablet
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
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LanguageControlPanel(
                                language = language,
                                onSetLanguage = onSetLanguage,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ReduceMotionControlPanel(
                                language = language,
                                reduceMotion = reduceMotion,
                                onSetReduceMotion = onSetReduceMotion,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        SystemEngineMatrixPanel(
                            language = language,
                            reduceMotion = reduceMotion,
                            sessionHistoryCount = sessionHistoryCount,
                            onClearHistory = onClearHistory,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LanguageControlPanel(
                            language = language,
                            onSetLanguage = onSetLanguage,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ReduceMotionControlPanel(
                            language = language,
                            reduceMotion = reduceMotion,
                            onSetReduceMotion = onSetReduceMotion,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SystemEngineMatrixPanel(
                            language = language,
                            reduceMotion = reduceMotion,
                            sessionHistoryCount = sessionHistoryCount,
                            onClearHistory = onClearHistory,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            AppUpdateStatusPanel(
                language = language,
                installedVersionName = installedVersionName,
                installedVersionCode = installedVersionCode,
                updateState = updateState,
                preferences = updatePreferences,
                onCheckForUpdates = onCheckForUpdates,
                onOpenUpdateDialog = onOpenUpdateDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp)
            )
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LanguageControlPanel(
    language: AppLanguage,
    onSetLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        borderColor = colors.borderCyan,
        showCornerReticles = true
    ) {
        Text(
            text = if (language == AppLanguage.UR) "انٹرفیس کی زبان" else "INTERFACE LANGUAGE",
            style = MaterialTheme.typography.labelMedium,
            color = colors.cyanPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (language == AppLanguage.UR)
                "زبان کی ترجیح صرف آپ کے آلے پر محفوظ کی جاتی ہے۔ کوئی پیغام محفوظ نہیں کیا جاتا۔"
            else
                "Language preference is stored locally on your device only. No submitted messages are ever persisted.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppLanguage.entries.forEach { lang ->
                val selected = language == lang
                OutlinedButton(
                    onClick = { onSetLanguage(lang) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("settings_lang_${lang.code}"),
                    border = BorderStroke(
                        if (selected) 1.5.dp else 1.dp,
                        if (selected) colors.cyanPrimary else colors.borderMuted
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) colors.cyanPrimary.copy(alpha = 0.14f) else colors.surfaceDeep,
                        contentColor = if (selected) colors.cyanPrimary else colors.textPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(lang.displayLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ReduceMotionControlPanel(
    language: AppLanguage,
    reduceMotion: Boolean,
    onSetReduceMotion: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == AppLanguage.UR) "حرکت میں کمی (Reduce Motion)" else "Reduce Motion & Ambient Animations",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.UR)
                        "بیک گراؤنڈ گرڈ، ریڈار گھماؤ اور اسکور اینیمیشن کو محدود کریں۔"
                    else
                        "Disable continuous grid drift, radar rotation, and sweep transitions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = reduceMotion,
                onCheckedChange = onSetReduceMotion,
                modifier = Modifier.testTag("settings_reduce_motion_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.bgPrimary,
                    checkedTrackColor = colors.cyanPrimary
                )
            )
        }
    }
}

@Composable
private fun SystemEngineMatrixPanel(
    language: AppLanguage,
    reduceMotion: Boolean,
    sessionHistoryCount: Int,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        containerColor = colors.surfaceDeep,
        accentRailColor = colors.limeSignal
    ) {
        Text(
            text = if (language == AppLanguage.UR) "انجن کی موجودہ حیثیت" else "SYSTEM ENGINE STATUS",
            style = MaterialTheme.typography.labelMedium,
            color = colors.cyanPrimary
        )
        Spacer(Modifier.height(12.dp))

        EngineStatusRow(
            title = if (language == AppLanguage.UR) "جیمینائی AI تجزیہ" else "Gemini AI Analysis",
            status = if (language == AppLanguage.UR) "فعال (Firebase AI Logic)" else "Connected (Gemini Developer API)",
            statusColor = colors.limeSignal,
            reduceMotion = reduceMotion
        )
        HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))
        EngineStatusRow(
            title = if (language == AppLanguage.UR) "سیکیورٹی تصدیق" else "App Integrity",
            status = if (language == AppLanguage.UR) "Firebase App Check فعال" else "App Check Active (No DB / No Auth)",
            statusColor = colors.cyanPrimary,
            reduceMotion = reduceMotion
        )
        HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))
        EngineStatusRow(
            title = if (language == AppLanguage.UR) "مقامی URL انجن" else "Local URL Pattern Engine",
            status = if (language == AppLanguage.UR) "فعال • آف لائن ساختی جانچ" else "Active • Zero-Visit Inspection",
            statusColor = colors.limeSignal,
            reduceMotion = reduceMotion
        )
        HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))
        EngineStatusRow(
            title = if (language == AppLanguage.UR) "موجودہ سیشن ہسٹری" else "Volatile Session History",
            status = if (language == AppLanguage.UR) "$sessionHistoryCount اندراجات (صرف میموری)" else "$sessionHistoryCount item(s) in RAM only",
            statusColor = if (sessionHistoryCount > 0) colors.warnAmber else colors.textSecondary,
            reduceMotion = true
        )

        if (sessionHistoryCount > 0) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onClearHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("settings_clear_history_button"),
                border = BorderStroke(1.dp, colors.alertRed.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = colors.alertRed)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.UR) "سیشن ہسٹری صاف کریں" else "CLEAR SESSION HISTORY",
                    color = colors.alertRed
                )
            }
        }
    }
}

@Composable
private fun EngineStatusRow(
    title: String,
    status: String,
    statusColor: Color,
    reduceMotion: Boolean
) {
    val colors = LocalClickwiseColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
        AnimatedPulseBeacon(
            color = statusColor,
            reduceMotion = reduceMotion,
            size = 8.dp
        )
    }
}
