package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle

object ThemeService {
    val darkScheme = darkColorScheme(
        primary = Color(0xFF06B6D4), // Cian acento
        secondary = Color(0xFF0891B2),
        tertiary = Color(0xFF22D3EE),
        background = Color(0xFF020617), // Fondo
        surface = Color(0xFF0F172A),    // Card, Drawer backgrounds
        surfaceVariant = Color(0xFF1E293B),
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF8FAFC),
        onSurfaceVariant = Color(0xFFCBD5E1)
    )

    val lightScheme = lightColorScheme(
        primary = Color(0xFF1E3A8A), // Azul marino
        secondary = Color(0xFF1E293B), // Gris oscuro
        tertiary = Color(0xFF3B82F6),
        background = Color(0xFFFFFFFF), // Fondo totalmente blanco
        surface = Color(0xFFF1F5F9),    // Tarjetas en gris muy tenue
        surfaceVariant = Color(0xFFE2E8F0),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFF1E293B), // Textos principales negro/gris oscuro
        onSurface = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFF475569)
    )

    val blueSkyScheme = lightColorScheme(
        primary = Color(0xFF0284C7), // Azul brillante acento
        secondary = Color(0xFF2563EB),
        tertiary = Color(0xFF38BDF8),
        background = Color(0xFFE0F2FE), // Fondo celeste cielo pastel
        surface = Color(0xFFFFFFFF),    // Tarjetas y contenedores en blanco puro
        surfaceVariant = Color(0xFFF0F9FF),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFF0369A1), // Azul noche profundo
        onSurface = Color(0xFF0369A1), // Textos en azul noche profundo
        onSurfaceVariant = Color(0xFF0284C7)
    )
}

fun scaleTextStyle(style: TextStyle, multiplier: Float): TextStyle {
    val currentSize = style.fontSize
    val nextSize = if (currentSize.toString().endsWith("sp")) {
        (currentSize.value * multiplier).sp
    } else {
        currentSize
    }
    
    val currentLineHeight = style.lineHeight
    val nextLineHeight = if (currentLineHeight.toString().endsWith("sp")) {
        (currentLineHeight.value * multiplier).sp
    } else {
        currentLineHeight
    }
    
    return style.copy(fontSize = nextSize, lineHeight = nextLineHeight)
}

fun scaleTypography(base: androidx.compose.material3.Typography, multiplier: Float): androidx.compose.material3.Typography {
    return androidx.compose.material3.Typography(
        displayLarge = scaleTextStyle(base.displayLarge, multiplier),
        displayMedium = scaleTextStyle(base.displayMedium, multiplier),
        displaySmall = scaleTextStyle(base.displaySmall, multiplier),
        headlineLarge = scaleTextStyle(base.headlineLarge, multiplier),
        headlineMedium = scaleTextStyle(base.headlineMedium, multiplier),
        headlineSmall = scaleTextStyle(base.headlineSmall, multiplier),
        titleLarge = scaleTextStyle(base.titleLarge, multiplier),
        titleMedium = scaleTextStyle(base.titleMedium, multiplier),
        titleSmall = scaleTextStyle(base.titleSmall, multiplier),
        bodyLarge = scaleTextStyle(base.bodyLarge, multiplier),
        bodyMedium = scaleTextStyle(base.bodyMedium, multiplier),
        bodySmall = scaleTextStyle(base.bodySmall, multiplier),
        labelLarge = scaleTextStyle(base.labelLarge, multiplier),
        labelMedium = scaleTextStyle(base.labelMedium, multiplier),
        labelSmall = scaleTextStyle(base.labelSmall, multiplier)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    secondary = Slate80,
    tertiary = Teal80,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color(0xFF022C22),
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    secondary = Slate40,
    tertiary = Teal40,
    background = Color(0xFFFBFEFC), // Very gentle warm white
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: String = "LIGHT", // "DARK", "LIGHT", "BLUE SKY"
    fontSizeScale: String = "Mediano",
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set false to force our specific fatigue-reducing theme!
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeColor) {
        "DARK" -> ThemeService.darkScheme
        "LIGHT" -> ThemeService.lightScheme
        "BLUE SKY" -> ThemeService.blueSkyScheme
        else -> ThemeService.lightScheme
    }

    val multiplier = when (fontSizeScale) {
        "Pequeño" -> 0.85f
        "Grande" -> 1.15f
        else -> 1.0f
    }
    
    val scaledTypography = scaleTypography(Typography, multiplier)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
