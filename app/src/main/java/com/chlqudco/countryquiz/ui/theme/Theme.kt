package com.chlqudco.countryquiz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OceanGreenDark,
    onPrimary = Color(0xFF003735),
    primaryContainer = Color(0xFF00504C),
    onPrimaryContainer = Color(0xFFA2F2EA),
    secondary = Color(0xFF9DCCDA),
    tertiary = Color(0xFFFFB4A6),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF253236),
    onBackground = Color(0xFFE1E9E8),
    onSurface = Color(0xFFE1E9E8)
)

private val LightColorScheme = lightColorScheme(
    primary = OceanGreen,
    onPrimary = Color.White,
    primaryContainer = SoftMint,
    onPrimaryContainer = Color(0xFF003735),
    secondary = SkyBlue,
    tertiary = Coral,
    background = WarmSand,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F4F3),
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = MutedInk,
    error = Color(0xFFB3261E)
)

@Composable
fun CountryQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
