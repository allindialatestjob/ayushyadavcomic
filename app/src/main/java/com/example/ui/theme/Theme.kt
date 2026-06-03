package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CozyDarkPrimary,
    secondary = CozyDarkSecondary,
    tertiary = CozyDarkTertiary,
    background = CozyDarkBackground,
    surface = CozyDarkSurface,
    onPrimary = CozyDarkBackground,
    onSecondary = CozyDarkOnBackground,
    onTertiary = CozyDarkOnBackground,
    onBackground = CozyDarkOnBackground,
    onSurface = CozyDarkOnSurface,
    outlineVariant = CozyDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantPrimary,
    secondary = VibrantSecondary,
    tertiary = VibrantTertiary,
    background = VibrantBackground,
    surface = VibrantSurface,
    onPrimary = VibrantBackground,
    onSecondary = VibrantOnBackground,
    onTertiary = VibrantOnBackground,
    onBackground = VibrantOnBackground,
    onSurface = VibrantOnSurface,
    outlineVariant = VibrantBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to strictly enforce our curated "Vibrant Palette" identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
