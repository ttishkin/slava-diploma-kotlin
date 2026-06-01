package com.nk.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Фирменные цвета Невский Кондитер
val NkGreen = Color(0xFFC7F94B)
val NkDark = Color(0xFF1A1A2E)
val NkDarkSurface = Color(0xFF16213E)
val NkDarkCard = Color(0xFF1E2D4A)
val NkRed = Color(0xFFFF375F)
val NkOrange = Color(0xFFF2A65A)
val NkBlue = Color(0xFF5E5CE6)

private val DarkColors = darkColorScheme(
    primary = NkGreen,
    onPrimary = Color.Black,
    secondary = NkOrange,
    tertiary = NkBlue,
    background = NkDark,
    surface = NkDarkSurface,
    surfaceVariant = NkDarkCard,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0C0),
    error = NkRed,
    outline = Color(0xFF3A3A5C)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    secondary = NkOrange,
    tertiary = NkBlue,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEEEEE),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF666666),
    error = NkRed,
    outline = Color(0xFFDDDDDD)
)

@Composable
fun NkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
