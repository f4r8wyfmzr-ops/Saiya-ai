package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SiyaDarkColorScheme = darkColorScheme(
    primary = ElectricAmethyst,
    onPrimary = ObsidianBlack,
    primaryContainer = DeepRoyalPurple,
    onPrimaryContainer = SoftLavender,
    secondary = CyberCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = CyanMuted,
    onSecondaryContainer = TextPrimary,
    tertiary = RadiantOrchid,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = DeepCosmicPurple,
    onSurface = TextPrimary,
    surfaceVariant = MidnightCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = ElevatedVioletSurface,
    outline = VioletBorder,
    outlineVariant = VioletBorder.copy(alpha = 0.5f),
    error = ListeningRose,
    onError = ObsidianBlack,
)

val SiyaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SiyaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SiyaDarkColorScheme,
        typography = SiyaTypography,
        shapes = SiyaShapes,
        content = content
    )
}
