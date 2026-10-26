package com.nextai.camera.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nextai.camera.R

val NextAiDigitalClockFontFamily = FontFamily(
    Font(R.font.roboto_medium_numbers, FontWeight.Bold)
)

val NextAiTelemetryMonoFontFamily = FontFamily.Monospace

val NextAiTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = NextAiDigitalClockFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NextAiTelemetryMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp
    )
)
