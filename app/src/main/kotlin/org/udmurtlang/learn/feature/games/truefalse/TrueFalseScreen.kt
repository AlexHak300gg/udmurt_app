package org.udmurtlang.learn.feature.games.truefalse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.components.ChoiceTile
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.SoundButton
import org.udmurtlang.learn.core.designsystem.components.TileState
import org.udmurtlang.learn.core.designsystem.chromeOnVivid
import org.udmurtlang.learn.core.designsystem.chromeOnVividText
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameScaffold
import org.udmurtlang.learn.feature.games.GameTintTrueFalse

@Composable
fun TrueFalseScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: TrueFalseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is GameEffect.Finished) {
                onFinished(effect.score, effect.correct, effect.total)
            }
        }
    }

    GameScaffold(
        title = stringResource(R.string.game_truefalse),
        round = state.index + 1,
        total = state.rounds.size.coerceAtLeast(TrueFalseViewModel.ROUNDS),
        correct = state.correctCount,
        feedback = state.feedback,
        finished = state.finished,
        onBack = onBack,
        tint = GameTintTrueFalse,
    ) {
        val round = state.current
        if (round == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.game_truefalse),
                )
            }
            return@GameScaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(round.emoji, fontSize = MaterialTheme.typography.displayLarge.fontSize)
            Text(
                text = round.udm,
                style = MaterialTheme.typography.displaySmall,
                color = onVivid(),
            )
            SoundButton(
                onClick = viewModel::speakWord,
                contentDescription = stringResource(R.string.pronunciation),
                container = onVividMuted(),
                contentColor = onVivid(),
                modifier = Modifier.padding(vertical = 10.dp),
            )
            Text(
                text = "${stringResource(R.string.word_translation)}: ${round.ru}",
                style = MaterialTheme.typography.titleMedium,
                color = onVivid(),
                modifier = Modifier.padding(bottom = 24.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ChoiceTile(
                    label = stringResource(R.string.true_label),
                    emoji = "✅",
                    state = when {
                        state.feedback == null -> TileState.NEUTRAL
                        round.isTrue -> TileState.CORRECT
                        else -> TileState.WRONG
                    },
                    enabled = !state.locked,
                    onClick = { viewModel.onIntent(TrueFalseIntent.Answer(clickedTrue = true)) },
                    modifier = Modifier.weight(1f),
                )
                ChoiceTile(
                    label = stringResource(R.string.false_label),
                    emoji = "❌",
                    state = when {
                        state.feedback == null -> TileState.NEUTRAL
                        !round.isTrue -> TileState.CORRECT
                        else -> TileState.WRONG
                    },
                    enabled = !state.locked,
                    onClick = { viewModel.onIntent(TrueFalseIntent.Answer(clickedTrue = false)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
