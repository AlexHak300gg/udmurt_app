package org.udmurtlang.learn.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

enum class WindowClass { COMPACT, MEDIUM, EXPANDED }

val WideContentWidth = 1120.dp
val ReadingContentWidth = 720.dp

private val LocalViewportSize = compositionLocalOf<DpSize?> { null }

@Composable
fun AppViewport(content: @Composable () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        ),
    ) {
        val density = LocalDensity.current
        val topInset = with(density) { WindowInsets.safeDrawing.getTop(density).toDp() }
        CompositionLocalProvider(LocalViewportSize provides DpSize(maxWidth, maxHeight - topInset)) {
            content()
        }
    }
}

@Composable
fun Modifier.safeContentTopPadding(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))

@Composable
fun currentWindowClass(): WindowClass {
    val width = LocalViewportSize.current?.width ?: LocalConfiguration.current.screenWidthDp.dp
    val widthDp = width.value / LocalDensity.current.fontScale.coerceAtLeast(1f)
    return when {
        widthDp < 600 -> WindowClass.COMPACT
        widthDp < 840 -> WindowClass.MEDIUM
        else -> WindowClass.EXPANDED
    }
}

val WindowClass.isWide: Boolean get() = this != WindowClass.COMPACT

@Composable
fun isShortWindow(): Boolean {
    val height = LocalViewportSize.current?.height ?: LocalConfiguration.current.screenHeightDp.dp
    return height / LocalDensity.current.fontScale.coerceAtLeast(1f) < 480.dp
}

fun WindowClass.columns(): Int = when (this) {
    WindowClass.COMPACT -> 1
    WindowClass.MEDIUM -> 2
    WindowClass.EXPANDED -> 3
}

val WindowClass.gridSpacing: Dp get() = if (this == WindowClass.COMPACT) 12.dp else 16.dp

@Composable
fun AdaptivePanes(
    modifier: Modifier = Modifier,
    sideBySide: Boolean = currentWindowClass().isWide,
    firstWeight: Float = 1f,
    secondWeight: Float = 1f,
    spacing: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
) {
    if (sideBySide) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = verticalAlignment,
        ) {
            Box(Modifier.weight(firstWeight), contentAlignment = Alignment.Center) { first() }
            Box(Modifier.weight(secondWeight), contentAlignment = Alignment.Center) { second() }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            first()
            second()
        }
    }
}

/** Keeps text and controls readable on wide windows and reachable on short ones. */
@Composable
fun ScrollableContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = ReadingContentWidth,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    centerVertically: Boolean = false,
    fillViewport: Boolean = centerVertically,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        val minHeight = if (fillViewport && constraints.hasBoundedHeight) maxHeight else 0.dp
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minHeight)
                .padding(contentPadding),
            verticalArrangement = if (centerVertically) Arrangement.Center else Arrangement.Top,
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}
