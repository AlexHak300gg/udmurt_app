package org.udmurtlang.learn.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

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
private val LocalDarkThemeProgress = compositionLocalOf { 0f }

/** Цветной экран: в светлой теме — яркая заливка, в тёмной — нейтральный тёмный фон. */
@Composable
fun vividScreen(vivid: Color): Color =
    lerp(vivid, ZcDarkBg, LocalDarkThemeProgress.current)

@Composable
fun brandAccent(color: Color): Color {
    val dark = when (color) {
        ZcRed -> ZcRedDark
        ZcTeal, ZcTealDeep -> ZcTealDark
        ZcPurple, ZcPurpleTile -> ZcPurpleDark
        ZcOrange -> ZcOrangeDark
        else -> color
    }
    return lerp(color, dark, LocalDarkThemeProgress.current)
}

@Composable
fun studyCardColor(): Color = lerp(ZcCard, ZcDarkElevated, LocalDarkThemeProgress.current)

/** Текст поверх цветного экрана (светлая тема — белый, тёмная — светлый). */
@Composable
fun onVivid(): Color = lerp(Color.White, ZcDarkInk, LocalDarkThemeProgress.current)

@Composable
fun onVividMuted(): Color =
    lerp(Color.White.copy(alpha = 0.72f), ZcDarkMuted, LocalDarkThemeProgress.current)

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
    primary = ZcRedDark,
    onPrimary = ZcWhite,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = ZcTealDark,
    onSecondary = ZcWhite,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = ZcOrangeDark,
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
    inversePrimary = ZcRedOnDark,
)

private fun animatedColors(progress: Float): ColorScheme {
    if (progress <= 0f) return LightColors
    if (progress >= 1f) return DarkColors

    fun blend(light: Color, dark: Color) = lerp(light, dark, progress)
    fun foreground(background: Color, onLight: Color = ZcInk, onDark: Color = ZcDarkInk) =
        if (background.luminance() > 0.18f) onLight else onDark

    val base = if (progress < 0.5f) LightColors else DarkColors
    val background = blend(LightColors.background, DarkColors.background)
    val surface = blend(LightColors.surface, DarkColors.surface)
    val surfaceVariant = blend(LightColors.surfaceVariant, DarkColors.surfaceVariant)
    val primaryContainer = blend(LightColors.primaryContainer, DarkColors.primaryContainer)
    val secondaryContainer = blend(LightColors.secondaryContainer, DarkColors.secondaryContainer)
    val tertiaryContainer = blend(LightColors.tertiaryContainer, DarkColors.tertiaryContainer)
    val inverseSurface = blend(LightColors.inverseSurface, DarkColors.inverseSurface)
    val error = blend(LightColors.error, DarkColors.error)
    return base.copy(
        primary = blend(LightColors.primary, DarkColors.primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = foreground(primaryContainer, LightOnPrimaryContainer, DarkOnPrimaryContainer),
        secondary = blend(LightColors.secondary, DarkColors.secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = foreground(secondaryContainer, LightOnSecondaryContainer, DarkOnSecondaryContainer),
        tertiary = blend(LightColors.tertiary, DarkColors.tertiary),
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = foreground(tertiaryContainer, LightOnTertiaryContainer, DarkOnTertiaryContainer),
        background = background,
        onBackground = foreground(background),
        surface = surface,
        onSurface = foreground(surface),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = foreground(surfaceVariant, ZcMuted, ZcDarkMuted),
        outline = blend(LightColors.outline, DarkColors.outline),
        outlineVariant = blend(LightColors.outlineVariant, DarkColors.outlineVariant),
        error = error,
        onError = foreground(error, DarkColors.onError, LightColors.onError),
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceVariant,
        surfaceContainerLow = surface,
        inverseSurface = inverseSurface,
        inverseOnSurface = foreground(inverseSurface, DarkColors.inverseOnSurface, LightColors.inverseOnSurface),
        inversePrimary = blend(LightColors.inversePrimary, DarkColors.inversePrimary),
    )
}

@Composable
fun ZecheburTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = if (darkTheme) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "themePalette",
    )
    MaterialTheme(
        colorScheme = animatedColors(progress),
        typography = ZecheburTypography,
        shapes = ZecheburShapes,
    ) {
        CompositionLocalProvider(
            LocalBrand provides (if (darkTheme) DarkBrand else LightBrand),
            LocalDarkThemeProgress provides progress,
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}
