package org.udmurtlang.learn.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class ZcBrand(
    val isDark: Boolean,
    // страницы в «цветных» экранах: светлые — яркие, тёмная тема — нейтральные
    val screenBg: Color,
    val screenOn: Color,
    val screenOnMuted: Color,
    val chrome: Color,      // чипы/кнопки поверх цветного экрана
    val chromeOn: Color,
)

private val LightBrand = ZcBrand(
    isDark = false,
    screenBg = ZcWhite,
    screenOn = ZcInk,
    screenOnMuted = ZcMuted,
    chrome = ZcCard,
    chromeOn = ZcInk,
)

private val DarkBrand = ZcBrand(
    isDark = true,
    screenBg = ZcDarkBg,
    screenOn = ZcDarkInk,
    screenOnMuted = ZcDarkMuted,
    chrome = ZcDarkSubtle,
    chromeOn = ZcDarkInk,
)

val LocalBrand = staticCompositionLocalOf { LightBrand }

/** Цветной экран: в светлой теме — яркая заливка, в тёмной — нейтральный тёмный фон. */
@Composable
fun vividScreen(vivid: Color): Color =
    if (LocalBrand.current.isDark) MaterialTheme.colorScheme.background else vivid

/** Текст поверх цветного экрана (светлая тема — белый, тёмная — светлый). */
@Composable
fun onVivid(): Color = if (LocalBrand.current.isDark) MaterialTheme.colorScheme.onBackground else Color.White

@Composable
fun onVividMuted(): Color =
    if (LocalBrand.current.isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.72f)

/** Поверх найденного фона чипы — белые в светлой, тёмно-серые в тёмной. */
@Composable
fun chromeOnVivid(): Color =
    if (LocalBrand.current.isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White

@Composable
fun chromeOnVividText(): Color =
    if (LocalBrand.current.isDark) MaterialTheme.colorScheme.onSurface else ZcInk

private val LightColors = lightColorScheme(
    primary = ZcRed,
    onPrimary = ZcWhite,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = ZcTeal,
    onSecondary = ZcWhite,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = ZcOrange,
    onTertiary = ZcWhite,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = ZcCream,
    onBackground = ZcInk,
    surface = ZcCard,
    onSurface = ZcInk,
    surfaceVariant = ZcSubtle,
    onSurfaceVariant = ZcMuted,
    outline = ZcHairline,
    outlineVariant = ZcHairline,
    error = ErrorSoft,
    onError = ZcWhite,
    surfaceContainer = ZcCard,
    surfaceContainerHigh = ZcSubtle,
    surfaceContainerLow = ZcCard,
    inverseSurface = ZcInkPill,
    inverseOnSurface = ZcWhite,
    inversePrimary = ZcRedTop,
)

private val DarkColors = darkColorScheme(
    primary = ZcRed,
    onPrimary = ZcWhite,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = ZcTeal,
    onSecondary = ZcWhite,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = ZcOrange,
    onTertiary = ZcWhite,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = ZcDarkBg,
    onBackground = ZcDarkInk,
    surface = ZcDarkCard,
    onSurface = ZcDarkInk,
    surfaceVariant = ZcDarkSubtle,
    onSurfaceVariant = ZcDarkMuted,
    outline = ZcDarkHairline,
    outlineVariant = ZcDarkHairline,
    error = Color(0xFFF1736D),
    onError = ZcDarkBg,
    surfaceContainer = ZcDarkCard,
    surfaceContainerHigh = ZcDarkSubtle,
    surfaceContainerLow = ZcDarkCard,
    inverseSurface = ZcDarkInk,
    inverseOnSurface = ZcDarkBg,
    inversePrimary = ZcRedTop,
)

@Composable
fun ZecheburTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ZecheburTypography,
        shapes = ZecheburShapes,
    ) {
        CompositionLocalProvider(
            LocalBrand provides (if (darkTheme) DarkBrand else LightBrand),
            content = content,
        )
    }
}
