package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.domain.model.AppLanguage
import com.example.domain.model.FindingSeverity
import com.example.domain.model.RiskLevel
import com.example.ui.theme.LocalClickwiseColors

/**
 * Signature CLICKWISE obsidian telemetry panel with subtle vertical surface gradient,
 * optional corner reticle ticks, and optional left/start severity rail.
 */
@Composable
fun TechnicalPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = LocalClickwiseColors.current.borderMuted,
    containerColor: Color = LocalClickwiseColors.current.surfaceElevated,
    cornerRadius: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    showCornerReticles: Boolean = false,
    accentRailColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalClickwiseColors.current
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            containerColor.copy(alpha = 0.96f),
                            colors.bgSecondary.copy(alpha = 0.92f)
                        )
                    )
                )
                .drawBehind {
                    // Delicate top-edge specular sheen
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                colors.borderHighlight,
                                Color.Transparent
                            )
                        ),
                        start = Offset(18.dp.toPx(), 1f),
                        end = Offset(size.width - 18.dp.toPx(), 1f),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (accentRailColor != null) {
                        drawLine(
                            color = accentRailColor,
                            start = Offset(1.5.dp.toPx(), 14.dp.toPx()),
                            end = Offset(1.5.dp.toPx(), size.height - 14.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    if (showCornerReticles) {
                        val len = 10.dp.toPx()
                        val pad = 7.dp.toPx()
                        val tickColor = colors.cyanPrimary.copy(alpha = 0.45f)
                        val sw = 1.2.dp.toPx()
                        // Top-left
                        drawLine(tickColor, Offset(pad, pad), Offset(pad + len, pad), sw)
                        drawLine(tickColor, Offset(pad, pad), Offset(pad, pad + len), sw)
                        // Top-right
                        drawLine(tickColor, Offset(size.width - pad, pad), Offset(size.width - pad - len, pad), sw)
                        drawLine(tickColor, Offset(size.width - pad, pad), Offset(size.width - pad, pad + len), sw)
                        // Bottom-left
                        drawLine(tickColor, Offset(pad, size.height - pad), Offset(pad + len, size.height - pad), sw)
                        drawLine(tickColor, Offset(pad, size.height - pad), Offset(pad, size.height - pad - len), sw)
                        // Bottom-right
                        drawLine(tickColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - len, size.height - pad), sw)
                        drawLine(tickColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - len), sw)
                    }
                }
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun PlaceholderAiNoticeBanner(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.warnAmber.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, colors.warnAmber.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = colors.warnAmber,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = ClickwiseStrings.placeholderNoticeTitle(language),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.warnAmber
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = ClickwiseStrings.placeholderNoticeBody(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun RiskLevelBadge(
    level: RiskLevel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val badgeColor = when (level) {
        RiskLevel.LOW -> colors.limeSignal
        RiskLevel.MEDIUM -> colors.warnAmber
        RiskLevel.HIGH -> colors.warnAmber
        RiskLevel.CRITICAL -> colors.alertRed
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.14f))
            .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(badgeColor)
        )
        Text(
            text = ClickwiseStrings.riskLabel(level, language),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = badgeColor
        )
    }
}

@Composable
fun SeverityPill(
    severity: FindingSeverity,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val pillColor = when (severity) {
        FindingSeverity.LOW -> colors.limeSignal
        FindingSeverity.MEDIUM -> colors.warnAmber
        FindingSeverity.HIGH -> colors.warnAmber
        FindingSeverity.CRITICAL -> colors.alertRed
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(pillColor.copy(alpha = 0.14f))
            .border(1.dp, pillColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(pillColor)
        )
        Text(
            text = severity.name,
            style = MaterialTheme.typography.labelSmall,
            color = pillColor
        )
    }
}

@Composable
fun SectionEyebrow(
    text: String,
    accentColor: Color = LocalClickwiseColors.current.cyanPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(2.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(accentColor, accentColor.copy(alpha = 0.25f))
                    )
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accentColor
        )
    }
}
