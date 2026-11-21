package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.ui.theme.LocalClickwiseColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Live telemetry status dot with a breathing halo ring.
 */
@Composable
fun AnimatedPulseBeacon(
    color: Color,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp
) {
    val pulse = if (reduceMotion) {
        0.55f
    } else {
        val transition = rememberInfiniteTransition(label = "beacon")
        val anim by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "beaconAlpha"
        )
        anim
    }

    Box(modifier = modifier.size(size * 1.8f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size * 1.8f)) {
            drawCircle(
                color = color.copy(alpha = pulse * 0.28f),
                radius = this.size.minDimension / 2f
            )
            drawCircle(
                color = color,
                radius = (this.size.minDimension / 2f) * 0.45f
            )
        }
    }
}

/**
 * Bespoke vector diagram breaking a URL into Scheme, Subdomain, Root Host, and Path nodes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UrlAnatomyDiagram(
    scheme: String,
    host: String,
    subdomains: String,
    pathAndQuery: String,
    language: AppLanguage,
    hasWarnings: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val hostColor = if (hasWarnings) colors.warnAmber else colors.cyanPrimary
    val segments = listOf(
        Triple(if (language == AppLanguage.UR) "پروٹوکول" else "SCHEME", scheme.ifBlank { "—" }, colors.indigoAccent),
        Triple(if (language == AppLanguage.UR) "سب ڈومین" else "SUBDOMAIN", subdomains, if (subdomains != "None") colors.warnAmber else colors.textSecondary),
        Triple(if (language == AppLanguage.UR) "اصل ہوسٹ" else "ROOT HOST", host, hostColor),
        Triple(if (language == AppLanguage.UR) "پاتھ" else "PATH", pathAndQuery.take(28), colors.limeSignal)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bgPrimary.copy(alpha = 0.72f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (language == AppLanguage.UR) "بصری ساختی نقشہ" else "STRUCTURAL ANATOMY TRACE",
            style = MaterialTheme.typography.labelSmall,
            color = colors.cyanPrimary
        )

        // Vector connector bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val y = size.height / 2f
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(colors.indigoAccent, hostColor, colors.limeSignal)
                ),
                start = Offset(8.dp.toPx(), y),
                end = Offset(size.width - 8.dp.toPx(), y),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
            )
            val stops = listOf(0.06f, 0.35f, 0.65f, 0.94f)
            val dotColors = listOf(colors.indigoAccent, colors.warnAmber, hostColor, colors.limeSignal)
            stops.forEachIndexed { idx, frac ->
                val x = size.width * frac
                drawCircle(color = dotColors[idx].copy(alpha = 0.28f), radius = 6.dp.toPx(), center = Offset(x, y))
                drawCircle(color = dotColors[idx], radius = 3.dp.toPx(), center = Offset(x, y))
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { (label, value, tint) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tint.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, tint.copy(alpha = 0.34f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = tint
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom geometric lesson glyph per Cyber Academy module ID.
 */
@Composable
fun LessonTopicGlyph(
    lessonId: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val colors = LocalClickwiseColors.current
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Hexagonal telemetry frame
        val hex = Path().apply {
            for (i in 0 until 6) {
                val angle = (60 * i - 30) * PI / 180.0
                val px = cx + (cos(angle) * w * 0.45f).toFloat()
                val py = cy + (sin(angle) * h * 0.45f).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(hex, color = accentColor.copy(alpha = 0.12f))
        drawPath(hex, color = accentColor.copy(alpha = 0.55f), style = Stroke(width = 1.4.dp.toPx(), join = StrokeJoin.Round))

        when (lessonId) {
            "lesson_phishing" -> {
                // Hooked envelope vector
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(w * 0.28f, h * 0.34f),
                    size = Size(w * 0.44f, h * 0.30f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawLine(accentColor, Offset(w * 0.28f, h * 0.36f), Offset(cx, h * 0.52f), strokeWidth = 1.5.dp.toPx())
                drawLine(accentColor, Offset(w * 0.72f, h * 0.36f), Offset(cx, h * 0.52f), strokeWidth = 1.5.dp.toPx())
            }
            "lesson_social_engineering" -> {
                // Network manipulation nodes
                val p1 = Offset(cx, h * 0.30f)
                val p2 = Offset(w * 0.32f, h * 0.64f)
                val p3 = Offset(w * 0.68f, h * 0.64f)
                drawLine(accentColor.copy(alpha = 0.65f), p1, p2, strokeWidth = 1.4.dp.toPx())
                drawLine(accentColor.copy(alpha = 0.65f), p1, p3, strokeWidth = 1.4.dp.toPx())
                drawLine(accentColor.copy(alpha = 0.65f), p2, p3, strokeWidth = 1.4.dp.toPx())
                drawCircle(accentColor, radius = 3.dp.toPx(), center = p1)
                drawCircle(colors.warnAmber, radius = 3.dp.toPx(), center = p2)
                drawCircle(accentColor, radius = 3.dp.toPx(), center = p3)
            }
            "lesson_scholarships" -> {
                // Academic diamond / cap crest
                val crest = Path().apply {
                    moveTo(cx, h * 0.27f)
                    lineTo(w * 0.74f, cy)
                    lineTo(cx, h * 0.68f)
                    lineTo(w * 0.26f, cy)
                    close()
                }
                drawPath(crest, color = accentColor, style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round))
                drawCircle(accentColor, radius = 2.6.dp.toPx(), center = Offset(cx, cy))
            }
            "lesson_jobs" -> {
                // Briefcase outline
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(w * 0.28f, h * 0.38f),
                    size = Size(w * 0.44f, h * 0.28f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawArc(
                    color = accentColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.39f, h * 0.30f),
                    size = Size(w * 0.22f, h * 0.16f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            "lesson_otp" -> {
                // Padlock glyph
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(w * 0.32f, h * 0.46f),
                    size = Size(w * 0.36f, h * 0.24f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawArc(
                    color = accentColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.38f, h * 0.29f),
                    size = Size(w * 0.24f, h * 0.28f),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawCircle(accentColor, radius = 2.2.dp.toPx(), center = Offset(cx, h * 0.58f))
            }
            "lesson_urls" -> {
                // Chain / link nodes
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(w * 0.26f, h * 0.41f),
                    size = Size(w * 0.26f, h * 0.18f),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawRoundRect(
                    color = colors.limeSignal,
                    topLeft = Offset(w * 0.48f, h * 0.41f),
                    size = Size(w * 0.26f, h * 0.18f),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
            "lesson_passwords" -> {
                // Key icon
                drawCircle(accentColor, radius = w * 0.11f, center = Offset(w * 0.36f, cy), style = Stroke(width = 1.6.dp.toPx()))
                drawLine(accentColor, Offset(w * 0.47f, cy), Offset(w * 0.72f, cy), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accentColor, Offset(w * 0.62f, cy), Offset(w * 0.62f, h * 0.61f), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accentColor, Offset(w * 0.70f, cy), Offset(w * 0.70f, h * 0.58f), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
            }
            else -> {
                // Shield + meridian orbit
                drawCircle(accentColor, radius = w * 0.21f, center = Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))
                drawLine(accentColor, Offset(cx - w * 0.21f, cy), Offset(cx + w * 0.21f, cy), strokeWidth = 1.2.dp.toPx())
                drawLine(accentColor, Offset(cx, cy - h * 0.21f), Offset(cx, cy + h * 0.21f), strokeWidth = 1.2.dp.toPx())
            }
        }
    }
}

/**
 * Custom vector illustration for empty volatile session history vault.
 */
@Composable
fun VolatileVaultIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp
) {
    val colors = LocalClickwiseColors.current
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        drawCircle(
            brush = Brush.radialGradient(
                listOf(colors.cyanPrimary.copy(alpha = 0.16f), Color.Transparent)
            ),
            radius = w * 0.48f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = colors.borderCyan,
            radius = w * 0.40f,
            center = Offset(cx, cy),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        )
        drawRoundRect(
            color = colors.surfaceDeep,
            topLeft = Offset(w * 0.26f, h * 0.26f),
            size = Size(w * 0.48f, h * 0.48f),
            cornerRadius = CornerRadius(10.dp.toPx())
        )
        drawRoundRect(
            color = colors.cyanPrimary.copy(alpha = 0.7f),
            topLeft = Offset(w * 0.26f, h * 0.26f),
            size = Size(w * 0.48f, h * 0.48f),
            cornerRadius = CornerRadius(10.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx())
        )
        drawCircle(
            color = colors.cyanPrimary,
            radius = w * 0.11f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.8.dp.toPx())
        )
        drawCircle(
            color = colors.limeSignal,
            radius = w * 0.04f,
            center = Offset(cx, cy)
        )
    }
}

/**
 * Architectural pipeline vector diagram for About & Trust screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ZeroRetentionArchitectureDiagram(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val nodes = if (language == AppLanguage.UR) {
        listOf(
            Triple("01", "صارف کا ان پٹ", colors.textPrimary),
            Triple("02", "مقامی URL انجن", colors.limeSignal),
            Triple("03", "App Check تصدیق", colors.indigoAccent),
            Triple("04", "Gemini AI تجزیہ", colors.cyanPrimary)
        )
    } else {
        listOf(
            Triple("01", "Device Input", colors.textPrimary),
            Triple("02", "Local URL Parser", colors.limeSignal),
            Triple("03", "App Check Token", colors.indigoAccent),
            Triple("04", "Gemini AI Logic", colors.cyanPrimary)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgPrimary.copy(alpha = 0.76f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.UR) "سیکیورٹی آرکیٹیکچر فلو" else "ZERO-KEY VERIFICATION PIPELINE",
                style = MaterialTheme.typography.labelSmall,
                color = colors.cyanPrimary
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = colors.limeSignal.copy(alpha = 0.12f)
            ) {
                Text(
                    text = if (language == AppLanguage.UR) "کوئی ڈیٹابیس نہیں" else "NO DB / NO ACCOUNTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.limeSignal,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nodes.forEach { (step, title, tint) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(tint.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step,
                                style = MaterialTheme.typography.labelSmall,
                                color = tint
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Geometric sentinel rank crest for Security Quiz completion screen.
 */
@Composable
fun SentinelRankCrest(
    scoreRatio: Float,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp
) {
    val colors = LocalClickwiseColors.current
    val accent = when {
        scoreRatio >= 0.8f -> colors.limeSignal
        scoreRatio >= 0.55f -> colors.cyanPrimary
        else -> colors.warnAmber
    }
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        drawCircle(
            brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.24f), Color.Transparent)),
            radius = w * 0.48f,
            center = Offset(cx, cy)
        )

        val outerHex = Path().apply {
            for (i in 0 until 6) {
                val angle = (60 * i - 90) * PI / 180.0
                val px = cx + (cos(angle) * w * 0.40f).toFloat()
                val py = cy + (sin(angle) * h * 0.40f).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(outerHex, color = colors.surfaceDeep)
        drawPath(outerHex, color = accent, style = Stroke(width = 2.2.dp.toPx(), join = StrokeJoin.Round))

        val innerHex = Path().apply {
            for (i in 0 until 6) {
                val angle = (60 * i - 90) * PI / 180.0
                val px = cx + (cos(angle) * w * 0.27f).toFloat()
                val py = cy + (sin(angle) * h * 0.27f).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(innerHex, color = accent.copy(alpha = 0.16f))
        drawCircle(color = accent, radius = w * 0.08f, center = Offset(cx, cy))
    }
}
