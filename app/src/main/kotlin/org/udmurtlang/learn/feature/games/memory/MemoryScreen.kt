package org.udmurtlang.learn.feature.games.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.RoundChip
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameTintMemory

@Composable
fun MemoryScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: MemoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val brand = LocalBrand.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is GameEffect.Finished) {
                onFinished(effect.score, effect.correct, effect.total)
            }
        }
    }

    val matchedPairs = state.matchedWordIds.size
    val round = state.round
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ZcHeader(
            title = stringResource(R.string.game_memory),
            onBack = onBack,
            trailing = {
                RoundChip(
                    text = stringResource(R.string.memory_pairs, matchedPairs, MemoryViewModel.PAIR_COUNT),
                    dark = !brand.isDark,
                )
            },
        )
        if (round == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.game_memory),
                )
            }
            return@Column
        }
        Text(
            text = stringResource(R.string.memory_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                round.left.forEach { side ->
                    val matched = side.wordId in state.matchedWordIds
                    val selected = state.selectedLeft == side
                    val wrong = side.wordId in state.wrongWordIds
                    MemoryTile(
                        side = side,
                        matched = matched,
                        selected = selected,
                        wrong = wrong,
                        enabled = !state.locked && !matched,
                        onClick = { viewModel.onIntent(MemoryIntent.SelectLeft(side)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                round.right.forEach { side ->
                    val matched = side.wordId in state.matchedWordIds
                    val selected = state.selectedRight == side
                    val wrong = side.wordId in state.wrongWordIds
                    MemoryTile(
                        side = side,
                        matched = matched,
                        selected = selected,
                        wrong = wrong,
                        enabled = !state.locked && !matched,
                        onClick = { viewModel.onIntent(MemoryIntent.SelectRight(side)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryTile(
    side: org.udmurtlang.learn.domain.model.MemorySide,
    matched: Boolean,
    selected: Boolean,
    wrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = when {
        matched -> ZcTeal
        selected -> ZcRed
        else -> scheme.surface
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = if (matched || selected) Color.White else scheme.onSurface,
        modifier = modifier,
    ) {
        Box {
            if (matched && !LocalBrand.current.isDark) {
                StarPattern(
                    modifier = Modifier.matchParentSize(),
                    color = ZcWhite,
                    alpha = 0.12f,
                    cell = 44.dp,
                )
            }
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (side.isEmoji) {
                    Text(side.text, fontSize = 30.sp, textAlign = TextAlign.Center)
                } else {
                    Text(
                        text = side.text,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}
