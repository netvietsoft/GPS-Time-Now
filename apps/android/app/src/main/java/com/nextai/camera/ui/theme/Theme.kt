package com.nextai.camera.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NextAiDarkColorScheme = darkColorScheme(
    primary = NextAiPrimaryBlue,
    onPrimary = NextAiDarkTextPrimary,
    secondary = NextAiIndustrialAmber,
    onSecondary = NextAiDarkBackground,
    tertiary = NextAiVerifiedEmerald,
    background = NextAiDarkBackground,
    onBackground = NextAiDarkTextPrimary,
    surface = NextAiDarkSurface,
    onSurface = NextAiDarkTextPrimary,
    surfaceVariant = NextAiDarkSurfaceElevated,
    onSurfaceVariant = NextAiDarkTextSecondary,
    error = NextAiDangerRed
)

private val NextAiLightColorScheme = lightColorScheme(
    primary = NextAiPrimaryBlue,
    onPrimary = NextAiLightSurface,
    secondary = NextAiIndustrialAmber,
    onSecondary = NextAiLightTextPrimary,
    tertiary = NextAiVerifiedEmerald,
    background = NextAiLightBackground,
    onBackground = NextAiLightTextPrimary,
    surface = NextAiLightSurface,
    onSurface = NextAiLightTextPrimary,
    surfaceVariant = NextAiLightSurfaceElevated,
    onSurfaceVariant = NextAiLightTextSecondary,
    error = NextAiDangerRed
)

@Composable
fun NextAiCameraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NextAiDarkColorScheme else NextAiLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NextAiTypography,
        content = content
    )
}
