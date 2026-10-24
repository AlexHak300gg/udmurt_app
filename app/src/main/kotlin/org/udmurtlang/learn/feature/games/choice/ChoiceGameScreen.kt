package org.udmurtlang.learn.feature.games.choice

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.components.ChoiceTile
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.SoundButton
import org.udmurtlang.learn.core.designsystem.components.TileState
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameScaffold
import org.udmurtlang.learn.feature.games.GameTintChoice

@Composable
fun ChoiceGameScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: ChoiceGameViewModel = hiltViewModel(),
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
        title = stringResource(R.string.game_choice),
        round = state.index + 1,
        total = state.rounds.size.coerceAtLeast(ChoiceGameViewModel.ROUNDS),
        correct = state.correctCount,
        feedback = state.feedback,
        finished = state.finished,
        onBack = onBack,
        tint = GameTintChoice,
    ) {
        val round = state.current
        if (round == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.game_choice),
                )
            }
            return@GameScaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = round.promptEmoji,
                fontSize = 44.sp,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Text(
                text = round.promptUdm,
                style = MaterialTheme.typography.displayMedium,
                color = onVivid(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.how_translate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onVividMuted(),
                )
                SoundButton(
                    onClick = viewModel::speakPrompt,
                    contentDescription = stringResource(R.string.pronunciation),
                    container = onVividMuted(),
                    contentColor = onVivid(),
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            round.options.forEachIndexed { index, option ->
                val tileState = when {
                    state.selected != null && index == round.correctIndex -> TileState.CORRECT
                    state.selected == index -> TileState.WRONG
                    else -> TileState.NEUTRAL
                }
                ChoiceTile(
                    label = option,
                    state = tileState,
                    enabled = !state.locked,
                    onClick = { viewModel.onIntent(ChoiceIntent.Select(index)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                )
            }
        }
    }
}
