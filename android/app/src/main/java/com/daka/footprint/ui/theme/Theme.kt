package com.daka.footprint.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary = Color(0xFF4CAF50)
val GreenDark = Color(0xFF388E3C)
val OrangeAccent = Color(0xFFFF9800)
val BackgroundColor = Color(0xFFF5F5F5)
val SurfaceColor = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenDark,
    secondary = OrangeAccent,
    background = BackgroundColor,
    surface = SurfaceColor,
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121)
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    secondary = OrangeAccent
)

@Composable
fun DakaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}