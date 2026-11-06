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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.components.ChoiceTile
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.TileState
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameScaffold
import org.udmurtlang.learn.feature.games.GameTintSorting
import org.udmurtlang.learn.domain.model.UiLanguage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortingScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
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
        round = state.placedWordIds.size,
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
                    tinted = true,
                )
            }
            return@GameScaffold
        }
        val wide = currentWindowClass().isWide
        val isUdmurt = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.language == UiLanguage.UDMURT.tag
        val categoryNames = when (state.themeId) {
            "animals_plants" -> listOf(stringResource(R.string.sort_animals), stringResource(R.string.sort_plants))
            "edible" -> listOf(stringResource(R.string.sort_edible), stringResource(R.string.sort_inedible))
            else -> round.categoryNames
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.sort_drag_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = onVivid(),
            )
            AdaptivePanes(
                sideBySide = wide,
                verticalAlignment = Alignment.Top,
                first = {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        maxItemsInEachRow = if (wide) 2 else 1,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        round.items.forEachIndexed { index, item ->
                            val placed = item.wordId in state.placedWordIds
                            if (!placed) {
                                ChoiceTile(
                                    label = if (isUdmurt) item.udm else item.label,
                                    modifier = Modifier.weight(1f),
                                    emoji = item.emoji,
                                    state = if (state.selectedItemIndex == index) TileState.CORRECT else TileState.NEUTRAL,
                                    enabled = !state.locked,
                                    onClick = {
                                        onSpeak(item.udm)
                                        viewModel.onIntent(SortingIntent.ItemSelected(index))
                                    },
                                )
                            }
                        }
                    }
                },
                second = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoryNames.forEachIndexed { categoryIndex, name ->
                            Surface(
                                onClick = { viewModel.onIntent(SortingIntent.CategorySelected(categoryIndex)) },
                                enabled = !state.locked && state.selectedItemIndex != null,
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
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
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                ) {
                                                    Text(
                                                        text = "${item.emoji} ${if (isUdmurt) item.udm else item.label}",
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
                },
            )
        }
    }
}
