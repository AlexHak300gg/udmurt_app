package org.udmurtlang.learn.feature.games.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.studyCardColor
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.LetterTile
import org.udmurtlang.learn.core.designsystem.components.LetterTileKind
import org.udmurtlang.learn.core.designsystem.components.SoundButton
import org.udmurtlang.learn.core.media.AutoPronounce
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameScaffold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuilderScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: BuilderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AutoPronounce(state.current?.wordId, viewModel::speakWord)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is GameEffect.Finished) {
                onFinished(effect.score, effect.correct, effect.total)
            }
        }
    }

    GameScaffold(
        title = stringResource(R.string.game_builder),
        round = state.index + 1,
        total = state.rounds.size.coerceAtLeast(BuilderViewModel.ROUNDS),
        correct = state.correctCount,
        feedback = state.feedback,
        finished = state.finished,
        onBack = onBack,
        compactHeader = true,
        footer = {
            Row(
                modifier = Modifier
                    .widthIn(max = ReadingContentWidth).fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    onClick = { viewModel.onIntent(BuilderIntent.Undo) },
                    enabled = state.built.isNotEmpty() && !state.locked,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(54.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                GameButton(
                    text = stringResource(R.string.check_word),
                    enabled = state.built.isNotEmpty() && !state.locked,
                    onClick = { viewModel.onIntent(BuilderIntent.Check) },
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        val round = state.current
        if (round == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.builder_hint),
                )
            }
            return@GameScaffold
        }
        val short = isShortWindow()
        val tileSize = if (short) 48.dp else 52.dp
        val tileSpacing = if (short) 6.dp else 8.dp
        AdaptivePanes(
            firstWeight = 0.85f,
            secondWeight = 1.15f,
            first = {
                // Оранжевая карточка задания
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(36.dp))
                        .background(brandAccent(ZcOrange)),
                ) {
                    Ornament(
                        kind = UdmOrnament.STAR,
                        color = ZcWhite.copy(alpha = 0.19f),
                        modifier = Modifier.align(Alignment.TopEnd).size(72.dp),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(shape = CircleShape, color = studyCardColor()) {
                            Box(Modifier.size(if (short) 58.dp else 82.dp), contentAlignment = Alignment.Center) {
                                Text(round.emoji, fontSize = if (short) 36.sp else 46.sp)
                            }
                        }
                        Text(
                            text = stringResource(R.string.game_builder_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                        Text(
                            text = round.ru,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    SoundButton(
                        onClick = viewModel::speakWord,
                        contentDescription = stringResource(R.string.pronunciation),
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    )
                }
            },
            second = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LetterGrid(state.letters.size, tileSize, tileSpacing) {
                        state.letters.forEachIndexed { index, _ ->
                            val char = state.built.getOrNull(index)
                            LetterTile(
                                letter = char ?: "",
                                kind = if (char == null) LetterTileKind.SLOT_EMPTY else LetterTileKind.SLOT_FILLED,
                                size = tileSize,
                                onClick = if (char != null && !state.locked) {
                                    { viewModel.onIntent(BuilderIntent.Undo) }
                                } else null,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (short) 10.dp else 20.dp))

                    Text(
                        text = stringResource(R.string.builder_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    LetterGrid(state.letters.size, tileSize, tileSpacing) {
                        state.letters.forEachIndexed { index, letter ->
                            val used = index in state.usedLetterIndexes
                            val kind = if (used) LetterTileKind.CANDIDATE_USED else LetterTileKind.CANDIDATE
                            LetterTile(
                                letter = letter,
                                kind = kind,
                                size = tileSize,
                                onClick = if (!used && !state.locked) {
                                    { viewModel.onIntent(BuilderIntent.LetterTapped(index)) }
                                } else null,
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun LetterGrid(
    count: Int,
    tileSize: Dp,
    spacing: Dp,
    content: @Composable FlowRowScope.() -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val capacity = ((maxWidth + spacing) / (tileSize + spacing)).toInt().coerceAtLeast(1)
        val rows = (count + capacity - 1) / capacity
        val columns = ((count + rows - 1) / rows.coerceAtLeast(1)).coerceAtLeast(1)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = columns,
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content,
        )
    }
}
