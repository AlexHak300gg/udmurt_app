package org.udmurtlang.learn.feature.games.sorting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.components.ChoiceTile
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.TileState
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameScaffold
import org.udmurtlang.learn.feature.games.GameTintSorting

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortingScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SortingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is GameEffect.Finished) {
                onFinished(effect.score, effect.correct, effect.total)
            }
        }
    }

    val round = state.round
    GameScaffold(
        title = stringResource(R.string.game_sorting),
        round = (round?.items?.size ?: 0) - state.remaining.size,
        total = round?.items?.size ?: 1,
        correct = state.correctCount,
        feedback = state.feedback,
        finished = state.finished,
        onBack = onBack,
        tint = GameTintSorting,
    ) {
        if (round == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.game_sorting),
                )
            }
            return@GameScaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.sort_drag_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = onVivid(),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                round.items.forEachIndexed { index, item ->
                    val placed = item.wordId in state.placedWordIds
                    if (!placed) {
                        ChoiceTile(
                            label = item.label,
                            emoji = item.emoji,
                            state = if (state.selectedItemIndex == index) TileState.CORRECT else TileState.NEUTRAL,
                            enabled = !state.locked,
                            onClick = { viewModel.onIntent(SortingIntent.ItemSelected(index)) },
                        )
                    }
                }
            }
            round.categoryNames.forEachIndexed { categoryIndex, name ->
                Surface(
                    onClick = { viewModel.onIntent(SortingIntent.CategorySelected(categoryIndex)) },
                    enabled = !state.locked && state.selectedItemIndex != null,
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            color = onVividMuted(),
                        )
                        val placedHere = round.items.filter {
                            it.wordId in state.placedWordIds && it.categoryIndex == categoryIndex
                        }
                        if (placedHere.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                placedHere.forEach { item ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = Color.White.copy(alpha = 0.9f),
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ) {
                                        Text(
                                            text = "${item.emoji} ${item.label}",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
