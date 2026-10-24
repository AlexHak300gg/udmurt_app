package org.udmurtlang.learn.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcPurpleDeep
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcBerry
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
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val brand = LocalBrand.current
    val bg = tint?.let { vividScreen(it) } ?: MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        if (!brand.isDark && tint != null) {
            StarPattern(modifier = Modifier.fillMaxSize(), color = Color.White, alpha = 0.07f)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            val roundText = stringResource(R.string.round_label, round.coerceAtMost(total.coerceAtLeast(1)), total)
            ZcHeader(
                title = title,
                caption = roundText,
                onBack = onBack,
                tinted = tint != null,
                trailing = { StarChip(count = correct, tinted = tint != null) },
            )
            if (total > 0) {
                ProgressLine(
                    progress = round.coerceIn(0, total).toFloat() / total,
                    fill = if (brand.isDark) MaterialTheme.colorScheme.primary else Color.White,
                    track = if (brand.isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            when (feedback) {
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
            content()
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
