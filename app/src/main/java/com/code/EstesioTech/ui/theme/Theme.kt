package com.code.EstesioTech.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definição das Cores (Baseado no seu colors.xml)
val BrandBackground = Color(0xFF101820)
val BrandPrimary = Color(0xFF00ACC1)
val BrandSecondary = Color(0xFF80DEEA)
val BrandTextHint = Color(0xFFB0B0B0)
val BrandBubbleReceived = Color(0xFFFF9800)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val GreenSuccess = Color(0xFF4CAF50)

// Esquema de Cores Escuro (Padrão do seu App)
private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandBubbleReceived,
    background = BrandBackground,
    surface = BrandBackground,
    onPrimary = White,
    onSecondary = Black,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandBubbleReceived,
    background = BrandBackground, // Forçando fundo escuro mesmo no modo light
    surface = BrandBackground,
    onPrimary = White,
    onSecondary = Black,
    onBackground = White,
    onSurface = White
)

@Composable
fun EstesioTechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está disponível no Android 12+
    dynamicColor: Boolean = false, // Desliguei para manter sua identidade visual
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // Usamos o mesmo esquema base escuro
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(), // Tipografia padrão por enquanto
        content = content
    )
}