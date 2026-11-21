package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalClickwiseColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Subtle animated technical grid + radial atmospheric cyan glow background.
 * GPU-friendly Canvas rendering; freezes movement when reduceMotion is true.
 */
@Composable
fun ClickwiseGridBackground(
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "bgAtmosphere")
    val gridShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridShift"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val effectiveShift = if (reduceMotion) 0f else gridShift
    val effectiveGlow = if (reduceMotion) 0.11f else glowPulse

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
    ) {
        val spacing = 48.dp.toPx()
        val shiftPx = (effectiveShift / 48f) * spacing

        // Delicate radial cyan light source at top-right
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.cyanPrimary.copy(alpha = effectiveGlow),
                    colors.bgSecondary.copy(alpha = 0.45f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.82f, size.height * 0.16f),
                radius = size.maxDimension * 0.65f
            )
        )

        // Subtle secondary deep indigo ambient glow bottom-left
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.indigoAccent.copy(alpha = 0.09f),
                    colors.cyanMuted.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.14f, size.height * 0.86f),
                radius = size.maxDimension * 0.58f
            )
        )

        val lineColor = colors.borderMuted.copy(alpha = 0.08f)
        val dotColor = colors.cyanPrimary.copy(alpha = 0.14f)

        var x = -spacing + (shiftPx % spacing)
        while (x < size.width + spacing) {
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += spacing
        }

        var y = -spacing + (shiftPx % spacing)
        while (y < size.height + spacing) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += spacing
        }

        // Fine dotted grid intersections
        var dx = -spacing + (shiftPx % spacing)
        while (dx < size.width + spacing) {
            var dy = -spacing + (shiftPx % spacing)
            while (dy < size.height + spacing) {
                drawCircle(
                    color = dotColor,
                    radius = 1.4.dp.toPx(),
                    center = Offset(dx, dy)
                )
                dy += spacing
            }
            dx += spacing
        }
    }
}

/**
 * Original CLICKWISE vector emblem combining a protective shield, scanning reticle, and digital eye.
 */
@Composable
fun ClickwiseShieldEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val colors = LocalClickwiseColors.current
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        val shieldPath = Path().apply {
            moveTo(cx, h * 0.06f)
            lineTo(w * 0.88f, h * 0.22f)
            lineTo(w * 0.84f, h * 0.56f)
            cubicTo(w * 0.80f, h * 0.76f, w * 0.64f, h * 0.88f, cx, h * 0.95f)
            cubicTo(w * 0.36f, h * 0.88f, w * 0.20f, h * 0.76f, w * 0.16f, h * 0.56f)
            lineTo(w * 0.12f, h * 0.22f)
            close()
        }

        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(colors.surfaceDeep, colors.bgSecondary)
            )
        )
        drawPath(
            path = shieldPath,
            color = colors.cyanPrimary,
            style = Stroke(width = w * 0.065f, join = StrokeJoin.Round)
        )

        // Scanning arc inside shield
        drawArc(
            color = colors.cyanPrimary.copy(alpha = 0.55f),
            startAngle = -45f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.24f, cy - h * 0.22f),
            size = Size(w * 0.48f, h * 0.48f),
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
        )

        // Digital verification eye contour
        val eyePath = Path().apply {
            moveTo(cx - w * 0.22f, cy + h * 0.02f)
            quadraticTo(cx, cy - h * 0.16f, cx + w * 0.22f, cy + h * 0.02f)
            quadraticTo(cx, cy + h * 0.20f, cx - w * 0.22f, cy + h * 0.02f)
            close()
        }
        drawPath(
            path = eyePath,
            color = colors.textPrimary.copy(alpha = 0.9f),
            style = Stroke(width = w * 0.045f, join = StrokeJoin.Round)
        )

        // Glowing iris core
        drawCircle(
            color = colors.cyanPrimary,
            radius = w * 0.075f,
            center = Offset(cx, cy + h * 0.02f)
        )
    }
}

@Composable
fun ClickwiseBrandWordmark(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 30.dp,
    showTagline: Boolean = false
) {
    val colors = LocalClickwiseColors.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClickwiseShieldEmblem(size = emblemSize)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "CLICKWISE",
                style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textPrimary
            )
            if (showTagline) {
                Text(
                    text = "THINK BEFORE YOU CLICK.",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = colors.cyanPrimary
                )
            }
        }
    }
}

/**
 * Brief branded entrance screen (<1s) with subtle cyan sweep. Tap to skip immediately.
 */
@Composable
fun ClickwiseBrandEntrance(
    visible: Boolean,
    reduceMotion: Boolean,
    onDismiss: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(if (reduceMotion) 100 else 350))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgPrimary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .testTag("brand_entrance_splash"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(colors.cyanPrimary.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    radius = size.minDimension * 0.42f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = colors.borderCyan.copy(alpha = 0.4f),
                    radius = 128.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ClickwiseShieldEmblem(size = 86.dp)
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "CLICKWISE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        letterSpacing = 4.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "THINK BEFORE YOU CLICK.",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.2.sp),
                    color = colors.cyanPrimary
                )
            }
        }
    }
}

/**
 * Original animated cybersecurity radar visualization with concentric rings,
 * rotating sweep beam, orbiting telemetry markers, and central shield-eye glyph.
 */
@Composable
fun CyberRadarVisualizer(
    isScanning: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val colors = LocalClickwiseColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "cyberRadar")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isScanning) 1800 else 6500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isScanning) 900 else 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulse"
    )

    val effectiveAngle = if (reduceMotion) 35f else sweepAngle
    val effectivePulse = if (reduceMotion) 0.55f else ringPulse

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val maxR = minOf(w, h) * 0.46f

            // Ambient backdrop disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.cyanPrimary.copy(alpha = if (isScanning) 0.18f else 0.10f),
                        colors.surfaceElevated.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = maxR * 1.1f
                ),
                radius = maxR * 1.05f,
                center = Offset(cx, cy)
            )

            // Concentric scanning rings
            val ringFractions = listOf(0.34f, 0.56f, 0.78f, 1.0f)
            ringFractions.forEachIndexed { idx, frac ->
                drawCircle(
                    color = colors.cyanPrimary.copy(
                        alpha = if (idx == ringFractions.lastIndex) 0.36f else 0.18f
                    ),
                    radius = maxR * frac,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (idx == 2) 1.5.dp.toPx() else 1.dp.toPx())
                )
            }

            // Crosshair axes
            drawLine(
                color = colors.borderMuted,
                start = Offset(cx - maxR, cy),
                end = Offset(cx + maxR, cy),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = colors.borderMuted,
                start = Offset(cx, cy - maxR),
                end = Offset(cx, cy + maxR),
                strokeWidth = 1.dp.toPx()
            )

            // Rotating radar sweep sector
            rotate(degrees = effectiveAngle, pivot = Offset(cx, cy)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.72f to Color.Transparent,
                        0.96f to colors.cyanPrimary.copy(alpha = if (isScanning) 0.42f else 0.24f),
                        1.0f to colors.cyanPrimary.copy(alpha = if (isScanning) 0.75f else 0.45f),
                        center = Offset(cx, cy)
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(cx - maxR, cy - maxR),
                    size = Size(maxR * 2f, maxR * 2f)
                )
            }

            // Orbiting technical markers
            val markerAngles = listOf(effectiveAngle + 25f, -effectiveAngle * 0.6f + 140f, effectiveAngle * 0.4f + 260f)
            val markerRadii = listOf(maxR * 0.56f, maxR * 0.78f, maxR * 1.0f)
            val markerColors = listOf(colors.cyanPrimary, colors.limeSignal, colors.warnAmber)
            markerAngles.forEachIndexed { i, deg ->
                val rad = deg * (PI / 180.0)
                val mx = cx + (cos(rad) * markerRadii[i]).toFloat()
                val my = cy + (sin(rad) * markerRadii[i]).toFloat()
                drawCircle(
                    color = markerColors[i].copy(alpha = effectivePulse),
                    radius = 6.dp.toPx(),
                    center = Offset(mx, my)
                )
                drawCircle(
                    color = markerColors[i],
                    radius = 3.dp.toPx(),
                    center = Offset(mx, my)
                )
            }
        }

        // Central shield emblem
        ClickwiseShieldEmblem(size = size * 0.28f)
    }
}
