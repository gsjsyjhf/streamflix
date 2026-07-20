package com.streamflix.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density

// Brand colors
val BrandPurple = Color(0xFF6366F1)
val BrandPurpleDark = Color(0xFF4F46E5)
val BrandPurpleLight = Color(0xFF818CF8)
val BrandRed = Color(0xFFE50914)
val BrandRedDark = Color(0xFFB20710)
val BrandGold = Color(0xFFF59E0B)
val BrandGreen = Color(0xFF22C55E)

// Dark theme
val DarkBg = Color(0xFF0A0E1A)
val DarkSurface = Color(0xFF141A2E)
val DarkSurfaceVariant = Color(0xFF1E2540)
val DarkOnBg = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

// Light theme
val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnBg = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF1E293B)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFFCBD5E1)

private val DarkColors = darkColorScheme(
    primary = BrandPurpleLight,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = BrandRed,
    onSecondary = Color.White,
    tertiary = BrandGold,
    background = DarkBg,
    onBackground = DarkOnBg,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFFCA5A5)
)

private val LightColors = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = BrandRed,
    onSecondary = Color.White,
    tertiary = BrandGold,
    background = LightBg,
    onBackground = LightOnBg,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = Color(0xFFDC2626)
)

val LocalFontScale = compositionLocalOf { 1.0f }

@Composable
fun StreamFlixTheme(
    darkTheme: Boolean = true,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val baseTypography = Typography()
    val typography = baseTypography.copy(
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        headlineLarge = baseTypography.headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        headlineMedium = baseTypography.headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
    )

    CompositionLocalProvider(
        LocalFontScale provides fontScale,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        val currentDensity = LocalDensity.current
        val scaledDensity = Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontScale
        )
        CompositionLocalProvider(LocalDensity provides scaledDensity) {
            MaterialTheme(
                colorScheme = colors,
                typography = typography,
                content = content
            )
        }
    }
}
