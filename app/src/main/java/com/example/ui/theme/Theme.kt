package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class ClickwiseColors(
    val bgPrimary: Color = BgPrimary,
    val bgSecondary: Color = BgSecondary,
    val surfaceElevated: Color = SurfaceElevated,
    val surfaceDeep: Color = SurfaceDeep,
    val surfaceHighlight: Color = SurfaceHighlight,
    val cyanPrimary: Color = CyanPrimary,
    val cyanMuted: Color = CyanMuted,
    val cyanGlow: Color = CyanGlow,
    val indigoAccent: Color = IndigoAccent,
    val limeSignal: Color = LimeSignal,
    val alertRed: Color = AlertRed,
    val warnAmber: Color = WarnAmber,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val borderMuted: Color = BorderMuted,
    val borderHighlight: Color = BorderHighlight,
    val borderCyan: Color = BorderCyan
)

val LocalClickwiseColors = staticCompositionLocalOf { ClickwiseColors() }

private val ClickwiseDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = BgPrimary,
    primaryContainer = SurfaceDeep,
    onPrimaryContainer = CyanPrimary,
    secondary = LimeSignal,
    onSecondary = BgPrimary,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = LimeSignal,
    tertiary = WarnAmber,
    onTertiary = BgPrimary,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = BgPrimary,
    surfaceContainerLow = BgSecondary,
    surfaceContainer = SurfaceElevated,
    surfaceContainerHigh = SurfaceDeep,
    outline = BorderMuted,
    outlineVariant = BorderMuted,
    error = AlertRed,
    onError = BgPrimary
)

val ClickwiseShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun ClickwiseTheme(
    content: @Composable () -> Unit
) {
    val customColors = ClickwiseColors()
    CompositionLocalProvider(LocalClickwiseColors provides customColors) {
        MaterialTheme(
            colorScheme = ClickwiseDarkColorScheme,
            typography = ClickwiseTypography,
            shapes = ClickwiseShapes,
            content = content
        )
    }
}
