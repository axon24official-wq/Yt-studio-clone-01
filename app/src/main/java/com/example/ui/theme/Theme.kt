package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Semantic color tokens for YouTube Studio
data class StudioColorTokens(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val chipBackground: Color,
    val chipSelectedBackground: Color,
    val chipSelectedText: Color,
    val chipBorder: Color,
    val searchBackground: Color,
    val navBackground: Color,
    val navSelected: Color,
    val navUnselected: Color,
    val accentRed: Color = StudioRed,
    val accentGreen: Color = StudioGreen,
    val accentTeal: Color = StudioCyan,
    val accentPurple: Color = StudioPurple,
    val isDark: Boolean = false
)

val LightStudioColors = StudioColorTokens(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFEEEEEE),
    divider = Color(0xFFEEEEEE),
    textPrimary = Color(0xFF0F0F0F),
    textSecondary = Color(0xFF606060),
    textTertiary = Color(0xFF909090),
    chipBackground = Color(0xFFF0F2F5),
    chipSelectedBackground = Color(0xFF0F0F0F),
    chipSelectedText = Color(0xFFFFFFFF),
    chipBorder = Color(0xFFD8DCE3),
    searchBackground = Color(0xFFF0F2F5),
    navBackground = Color(0xFFFFFFFF),
    navSelected = Color(0xFF0F0F0F),
    navUnselected = Color(0xFF606060),
    accentRed = StudioRed,
    accentGreen = StudioGreen,
    accentTeal = StudioCyan,
    accentPurple = StudioPurple,
    isDark = false
)

val DarkStudioColors = LightStudioColors

val LocalStudioColors = staticCompositionLocalOf { LightStudioColors }

object StudioTheme {
    val colors: StudioColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalStudioColors.current
}

private val LightM3ColorScheme = lightColorScheme(
    primary = StudioBlack,
    onPrimary = Color.White,
    primaryContainer = StudioCardBackground,
    onPrimaryContainer = StudioBlack,
    secondary = StudioSecondaryText,
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = StudioDarkText,
    surface = Color(0xFFFFFFFF),
    onSurface = StudioDarkText,
    surfaceVariant = StudioCardBackground,
    onSurfaceVariant = StudioSecondaryText,
    outline = StudioCardBorder,
    outlineVariant = StudioDivider
)

private val DarkM3ColorScheme = LightM3ColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val studioColors = if (darkTheme) DarkStudioColors else LightStudioColors
    val m3ColorScheme = if (darkTheme) DarkM3ColorScheme else LightM3ColorScheme

    CompositionLocalProvider(LocalStudioColors provides studioColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun StudioTheme(content: @Composable () -> Unit) {
    MyApplicationTheme(content = content)
}
