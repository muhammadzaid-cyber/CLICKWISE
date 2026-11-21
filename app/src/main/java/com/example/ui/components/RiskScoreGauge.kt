package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.domain.model.AppLanguage
import com.example.domain.model.RiskLevel
import com.example.ui.theme.LocalClickwiseColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RiskScoreGauge(
    score: Int,
    riskLevel: RiskLevel,
    language: AppLanguage,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 192.dp
) {
    val colors = LocalClickwiseColors.current
    val riskColor = when (riskLevel) {
        RiskLevel.LOW -> colors.limeSignal
        RiskLevel.MEDIUM -> colors.warnAmber
        RiskLevel.HIGH -> colors.warnAmber
        RiskLevel.CRITICAL -> colors.alertRed
    }

    val animatedScore = remember(score) { Animatable(if (reduceMotion) score.toFloat() else 0f) }

    LaunchedEffect(score, reduceMotion) {
        if (reduceMotion) {
            animatedScore.snapTo(score.toFloat())
        } else {
            animatedScore.snapTo(0f)
            animatedScore.animateTo(
                targetValue = score.toFloat(),
                animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
            )
        }
    }

    val currentVal = animatedScore.value
    val sweep = (currentVal / 100f).coerceIn(0f, 1f) * 260f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 12.dp.toPx()
            val inset = strokePx / 2f + 10.dp.toPx()
            val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val outerR = (this.size.minDimension / 2f) - 2.dp.toPx()

            // Outer calibrated telemetry ticks
            val tickCount = 26
            for (i in 0..tickCount) {
                val deg = 140f + (i.toFloat() / tickCount) * 260f
                val rad = deg * (PI / 180.0)
                val isMajor = i % 5 == 0
                val innerTickR = outerR - if (isMajor) 7.dp.toPx() else 4.dp.toPx()
                val start = Offset(cx + (cos(rad) * innerTickR).toFloat(), cy + (sin(rad) * innerTickR).toFloat())
                val end = Offset(cx + (cos(rad) * outerR).toFloat(), cy + (sin(rad) * outerR).toFloat())
                val tickFilled = (i.toFloat() / tickCount) * 100f <= currentVal
                drawLine(
                    color = if (tickFilled) riskColor.copy(alpha = 0.7f) else colors.borderMuted,
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 1.8.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Track arc
            drawArc(
                color = colors.surfaceDeep,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Subtle glow halo behind active arc
            if (sweep > 0f) {
                drawArc(
                    color = riskColor.copy(alpha = 0.22f),
                    startAngle = 140f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx + 7.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active progress arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to colors.cyanPrimary,
                        0.5f to riskColor,
                        1.0f to riskColor,
                        center = Offset(cx, cy)
                    ),
                    startAngle = 140f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        val compact = size < 170.dp
        Column(
            modifier = Modifier.padding(horizontal = if (compact) 22.dp else 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${currentVal.roundToInt()}",
                style = (if (compact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge)
                    .copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "/ 100",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = ClickwiseStrings.riskLabel(riskLevel, language),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = riskColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
