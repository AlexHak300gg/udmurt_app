package org.udmurtlang.learn.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcPurpleDeep
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcBerry
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.components.ConfettiOverlay
import org.udmurtlang.learn.core.designsystem.components.ProgressLine
import org.udmurtlang.learn.core.designsystem.components.StarChip
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.vividScreen

/* Цвета игр-экранов (светлая тема — яркие, тёмная — нейтральные) */
val GameTintChoice = ZcPurpleDeep
val GameTintOdd = ZcPurple
val GameTintSorting = ZcTeal
val GameTintMemory: Color? = null // крем
val GameTintBuilder: Color? = null // крем
val GameTintTrueFalse = ZcBerry

/** Общий каркас мини-игры: цветной фон, шапка раунда, счёт верных, фидбек, конфетти. */
@Composable
fun GameScaffold(
    title: String,
    round: Int,
    total: Int,
    correct: Int,
    feedback: FeedbackKind?,
    finished: Boolean,
    onBack: () -> Unit,
    tint: Color? = null,
    compactHeader: Boolean = false,
    showProgress: Boolean = true,
    patterned: Boolean = true,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val brand = LocalBrand.current
    val bg = tint?.let { vividScreen(it) } ?: MaterialTheme.colorScheme.background
    val short = isShortWindow()
    val wide = currentWindowClass().isWide
    val maxWidth = if (wide) WideContentWidth else ReadingContentWidth

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        if (patterned && !brand.isDark && tint != null) {
            StarPattern(modifier = Modifier.fillMaxSize(), color = Color.White, alpha = 0.07f)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentTopPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val roundText = stringResource(R.string.round_label, round.coerceAtMost(total.coerceAtLeast(1)), total)
            ZcHeader(
                title = title,
                caption = roundText,
                onBack = onBack,
                modifier = Modifier.widthIn(max = maxWidth),
                tinted = tint != null,
                compact = compactHeader,
                close = compactHeader,
                trailing = { StarChip(count = correct, tinted = tint != null) },
            )
            if (showProgress && total > 0) {
                ProgressLine(
                    progress = round.coerceIn(0, total).toFloat() / total,
                    fill = if (tint == null || brand.isDark) MaterialTheme.colorScheme.primary else Color.White,
                    track = if (tint == null || brand.isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.widthIn(max = maxWidth).padding(horizontal = 16.dp),
                )
            }
            when (feedback.takeIf { footer == null }) {
                FeedbackKind.CORRECT -> FeedbackBanner(
                    text = stringResource(R.string.correct_answer),
                    tinted = tint != null,
                    color = MaterialTheme.colorScheme.secondary,
                )
                FeedbackKind.WRONG -> FeedbackBanner(
                    text = stringResource(R.string.almost_answer),
                    tinted = tint != null,
                    color = MaterialTheme.colorScheme.error,
                )
                null -> Unit
            }
            ScrollableContent(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                maxWidth = maxWidth,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = if (short) 8.dp else 16.dp),
                centerVertically = wide,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
            footer?.invoke(this)
        }
    }
    ConfettiOverlay(active = finished)
}

@Composable
private fun FeedbackBanner(text: String, tinted: Boolean, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = if (tinted) onVivid() else color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}
