package org.udmurtlang.learn.feature.games.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.feature.games.GameEffect

@Composable
fun MemoryScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: MemoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val wide = currentWindowClass().isWide
    val short = isShortWindow()
    val scheme = MaterialTheme.colorScheme
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is GameEffect.Finished) onFinished(effect.score, effect.correct, effect.total)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(scheme.background).safeContentTopPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = stringResource(R.string.game_memory),
            onBack = onBack,
            modifier = Modifier.widthIn(max = ReadingContentWidth).padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.widthIn(max = ReadingContentWidth).fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MemoryCounter(
                stringResource(R.string.memory_moves, state.moves),
                brandAccent(ZcPurple).copy(alpha = 0.16f),
                scheme.onSurface,
            )
            MemoryCounter(
                stringResource(R.string.memory_pairs, state.matchedWordIds.size, state.cards.size / 2),
                scheme.secondaryContainer,
                scheme.onSecondaryContainer,
            )
        }
        ScrollableContent(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            centerVertically = wide,
        ) {
            if (state.cards.isEmpty()) {
                EmptyState(stringResource(R.string.loading), stringResource(R.string.game_memory))
                return@ScrollableContent
            }
            val columns = if (wide) 6 else 3
            val pairs = remember(state.cards) { state.cards.groupBy { it.wordId } }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.cards.indices.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { index ->
                            val card = state.cards[index]
                            val matched = card.wordId in state.matchedWordIds
                            val faceUp = matched || index in state.revealed
                            val label = stringResource(R.string.memory_card, index + 1)
                            val tileColor = when {
                                !faceUp -> brandAccent(ZcPurple)
                                matched && card.wordId.hashCode() % 2 == 0 -> scheme.tertiaryContainer
                                matched -> scheme.secondaryContainer
                                card.isEmoji -> scheme.tertiaryContainer
                                else -> scheme.secondaryContainer
                            }
                            Surface(
                                modifier = Modifier.weight(1f).then(
                                    if (faceUp) Modifier else Modifier.semantics { contentDescription = label },
                                ),
                                onClick = { viewModel.reveal(index) },
                                enabled = !faceUp && state.revealed.size < 2 && !state.finished,
                                shape = RoundedCornerShape(23.dp),
                                color = tileColor,
                                contentColor = scheme.onSurface,
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().heightIn(min = if (short) 64.dp else 94.dp)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (matched) {
                                        val pair = pairs.getValue(card.wordId)
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(pair.first { it.isEmoji }.text, fontSize = if (short) 20.sp else 28.sp)
                                            Text(
                                                pair.first { !it.isEmoji }.text,
                                                style = MaterialTheme.typography.labelMedium,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    } else if (faceUp) {
                                        Text(
                                            text = card.text,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontSize = if (card.isEmoji) 28.sp else 15.sp,
                                            textAlign = TextAlign.Center,
                                        )
                                    } else {
                                        val ornaments = UdmOrnament.entries
                                        Ornament(
                                            kind = ornaments[index % ornaments.size],
                                            color = Color.White,
                                            modifier = Modifier.size(32.dp),
                                        )
                                    }
                                }
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        Text(
            stringResource(R.string.memory_hint),
            color = scheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )
        OrnamentBand(
            colors = listOf(ZcRed, ZcOrange, ZcTeal, ZcPurple, scheme.onSurface, ZcRed),
            height = 21.dp,
            diamondSize = 18.dp,
            modifier = Modifier.widthIn(max = 180.dp).padding(bottom = 5.dp),
        )
    }
}

@Composable
private fun MemoryCounter(text: String, color: Color, contentColor: Color) {
    Surface(shape = CircleShape, color = color, contentColor = contentColor) {
        Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
    }
}
