package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.domain.model.AppLanguage
import com.example.domain.model.RiskLevel
import com.example.domain.model.SessionScanRecord
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.TechnicalPanel
import com.example.ui.components.VolatileVaultIllustration
import com.example.ui.theme.LocalClickwiseColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanHistoryScreen(
    records: List<SessionScanRecord>,
    language: AppLanguage,
    onOpenRecord: (SessionScanRecord) -> Unit,
    onClearHistory: () -> Unit,
    onGoToScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 980.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionEyebrow(text = ClickwiseStrings.historyHeadline(language))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.UR)
                            "رازداری کے تحفظ کے لیے مکمل پیغامات اور ہسٹری صرف موجودہ سیشن کی میموری میں رہتی ہے۔"
                        else
                            "For your privacy, session records remain strictly in volatile memory and are cleared when the app restarts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
                if (records.isNotEmpty()) {
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = onClearHistory,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("history_clear_button"),
                        border = BorderStroke(1.dp, colors.alertRed.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = null,
                            tint = colors.alertRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.UR) "صاف کریں" else "CLEAR",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.alertRed
                        )
                    }
                }
            }
        }

        if (records.isEmpty()) {
            item {
                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 980.dp),
                    borderColor = colors.borderCyan,
                    showCornerReticles = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VolatileVaultIllustration(size = 92.dp)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = if (language == AppLanguage.UR) "اس سیشن میں ابھی کوئی اسکین نہیں ہوا" else "No Scans Recorded This Session",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.UR)
                                "مکمل پیغامات مستقل محفوظ نہیں کیے جاتے۔ نیا تجزیہ شروع کرنے کے لیے اسکینر کھولیں۔"
                            else
                                "Full message bodies are never saved. Run a scan in the Message Scanner to review results here during your session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = onGoToScanner,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .height(48.dp)
                                .testTag("history_go_scanner_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cyanPrimary,
                                contentColor = colors.bgPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = ClickwiseStrings.startAnalysis(language),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        } else {
            items(records, key = { it.id }) { record ->
                val riskColor = when (record.riskLevel) {
                    RiskLevel.LOW -> colors.limeSignal
                    RiskLevel.MEDIUM, RiskLevel.HIGH -> colors.warnAmber
                    RiskLevel.CRITICAL -> colors.alertRed
                }
                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 980.dp)
                        .clickable { onOpenRecord(record) }
                        .testTag("history_item_${record.id}"),
                    borderColor = riskColor.copy(alpha = 0.35f),
                    accentRailColor = riskColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = colors.surfaceDeep
                                ) {
                                    Text(
                                        text = record.analysisType.name.replace('_', ' '),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = timeFormat.format(Date(record.timestampEpochMs)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.cyanPrimary
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (language == AppLanguage.UR) record.shortLabelUr else record.shortLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            // Mini risk gauge bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(colors.bgPrimary)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((record.riskScore.coerceIn(0, 100) / 100f))
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(riskColor)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            RiskLevelBadge(level = record.riskLevel, language = language)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${record.riskScore} / 100",
                                style = MaterialTheme.typography.labelLarge,
                                color = riskColor
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
