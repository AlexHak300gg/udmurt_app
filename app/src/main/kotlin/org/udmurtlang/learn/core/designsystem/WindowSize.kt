package org.udmurtlang.learn.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowClass { COMPACT, MEDIUM, EXPANDED }

/** Адаптивность: телефон / планшет / большой планшет. */
@Composable
fun currentWindowClass(): WindowClass {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> WindowClass.COMPACT
        widthDp < 840 -> WindowClass.MEDIUM
        else -> WindowClass.EXPANDED
    }
}

fun WindowClass.columns(default: Int = 1): Int = when (this) {
    WindowClass.COMPACT -> default
    WindowClass.MEDIUM -> maxOf(default, 2)
    WindowClass.EXPANDED -> maxOf(default, 3)
}

val WindowClass.gridSpacing: Dp get() = if (this == WindowClass.COMPACT) 12.dp else 16.dp
