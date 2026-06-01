package com.nk.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Фирменные цвета из PWA (CSS переменные)
val NkGreen = Color(0xFFC7F94B)        // --acc (акцент, лайм)
val NkGreenDark = Color(0xFF16240A)    // --accTx (текст на акценте)
val NkViolet = Color(0xFF8B7BFF)       // --violet / --nkA
val NkBlue = Color(0xFF5E5CE6)         // --nkB
val NkRed = Color(0xFFFF375F)          // --pink
val NkOrange = Color(0xFFFF9F0A)       // --orange
val NkSystemGreen = Color(0xFF34C759)  // --green

// Тёмная тема (по умолчанию в PWA)
val NkBg = Color(0xFF0E0F13)          // --bg
val NkBg2 = Color(0xFF15171D)         // --bg2
val NkCard = Color(0xFF181A21)        // --card
val NkCard2 = Color(0xFF1F222B)       // --card2
val NkLabel = Color(0xFFF2F3F6)       // --label
val NkLabel2 = Color(0xFF9CA2AE)      // --label2
val NkLabel3 = Color(0xFF5F6470)      // --label3
val NkSep = Color(0x17FFFFFF)         // --sep rgba(255,255,255,0.09)

// Цвета категорий
val CatCereal = Color(0xFFF2A65A)     // Злаковые батончики
val CatFruit = Color(0xFFFF6B6B)      // Фруктовый грильяж
val CatNut = Color(0xFFB5651D)        // Ореховые батончики
val CatProtein = Color(0xFF5E5CE6)    // Протеиновые батончики
val CatMarmalade = Color(0xFFFF375F)  // Мармелад
val CatJelly = Color(0xFF30B0C7)      // Желейные конфеты

// Цвета тегов
val TagSugarBg = Color(0x2934C759)
val TagSugarText = Color(0xFF5BE584)
val TagProtBg = Color(0x2E8B7BFF)
val TagProtText = Color(0xFFB3A8FF)
val TagFatBg = Color(0x2930B0C7)
val TagFatText = Color(0xFF5BD0E0)
val TagNutBg = Color(0x29E8A04A)
val TagNutText = Color(0xFFF0B25E)

// Цвета макронутриентов
val MacroProtein = Color(0xFF5E5CE6)  // Белки — фиолетовый
val MacroFat = Color(0xFFFF9F0A)      // Жиры — оранжевый
val MacroCarb = Color(0xFF34C759)     // Углеводы — зелёный

private val DarkColors = darkColorScheme(
    primary = NkGreen,
    onPrimary = NkGreenDark,
    secondary = NkOrange,
    onSecondary = Color.Black,
    tertiary = NkViolet,
    onTertiary = Color.White,
    background = NkBg,
    surface = NkBg2,
    surfaceVariant = NkCard,
    surfaceTint = Color.Transparent,
    surfaceBright = NkCard2,
    surfaceDim = NkBg,
    surfaceContainer = NkBg2,
    surfaceContainerLow = NkBg,
    surfaceContainerLowest = NkBg,
    surfaceContainerHigh = NkCard,
    surfaceContainerHighest = NkCard2,
    inverseSurface = NkLabel,
    inverseOnSurface = NkBg,
    inversePrimary = NkGreenDark,
    onBackground = NkLabel,
    onSurface = NkLabel,
    onSurfaceVariant = NkLabel2,
    error = NkRed,
    onError = Color.White,
    errorContainer = Color(0xFF3D0A15),
    onErrorContainer = NkRed,
    outline = NkSep,
    outlineVariant = NkSep,
    scrim = Color.Black
)

// Светлая тема из PWA
private val LightColors = lightColorScheme(
    primary = Color(0xFF2FA84F),      // --acc для светлой темы
    onPrimary = Color.White,
    secondary = NkOrange,
    tertiary = NkBlue,
    background = Color(0xFFF2F2F7),   // --bg для светлой
    surface = Color.White,            // --card
    surfaceVariant = Color(0xFFFFFFFF),
    onBackground = Color(0xFF11131A), // --label
    onSurface = Color(0xFF11131A),
    onSurfaceVariant = Color(0xFF6B7280), // --label2
    error = NkRed,
    outline = Color(0xFFE0E0E0)
)

// Типографика, приближённая к PWA
private val NkTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.W800, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.W800, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.W800, letterSpacing = (-0.4).sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W700, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.W600),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W600),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W600, lineHeight = 17.sp),
    bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.W400),
    bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.W400),
    bodySmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.W400),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W600),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W500),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.W500)
)

// Формы, приближённые к PWA (border-radius 18px/13px)
private val NkShapes = Shapes(
    extraSmall = RoundedCornerShape(9.dp),
    small = RoundedCornerShape(13.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun NkTheme(
    content: @Composable () -> Unit
) {
    // Принудительно тёмная тема (как в PWA по умолчанию)
    val colors = DarkColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = NkTypography,
        shapes = NkShapes,
        content = content
    )
}
