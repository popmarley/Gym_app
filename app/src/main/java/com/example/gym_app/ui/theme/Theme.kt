package com.example.gym_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = Night,
    secondary = Sky,
    tertiary = Sand,
    background = Night,
    onBackground = TextLight,
    surface = NightSurface,
    onSurface = TextLight,
    surfaceVariant = NightSurfaceRaised,
    onSurfaceVariant = TextMuted,
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightSurfaceRaised,
    primaryContainer = NightSurfaceBright,
    outline = Sky.copy(alpha = 0.25f)
)

private val LightColorScheme = lightColorScheme(
    primary = MintDark,
    onPrimary = TextLight,
    secondary = Sky,
    tertiary = Sand,
    background = TextLight,
    onBackground = Night,
    surface = Color.White,
    onSurface = Night,
    surfaceVariant = Color(0xFFF0F4F8),
    onSurfaceVariant = Color(0xFF445464),
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerHigh = Color(0xFFEFF4F8),
    primaryContainer = Color(0xFFD8F6EA),
    outline = Sky.copy(alpha = 0.2f)
)

@Composable
fun Gym_appTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme || dynamicColor) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
