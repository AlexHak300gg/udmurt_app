package org.udmurtlang.learn.feature.games.choice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.media.hasBundledSpeech
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.studyCardColor
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.WindowClass
import org.udmurtlang.learn.core.media.AutoPronounce
import org.udmurtlang.learn.core.designsystem.components.ChoiceTile
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.SoundButton
import org.udmurtlang.learn.core.designsystem.components.TileState
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.FeedbackKind
import org.udmurtlang.learn.feature.games.GameScaffold
import org.udmurtlang.learn.feature.games.GameTintChoice

@Composable
fun ChoiceGameScreen(
    onFinished: (Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: ChoiceGameViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AutoPronounce(state.current?.wordId, viewModel::speakPrompt)

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
        compactHeader = true,
        showProgress = false,
        patterned = false,
        footer = {
            val feedback = state.feedback
            if (feedback != null) {
                Text(
                    text = stringResource(if (feedback == FeedbackKind.CORRECT) R.string.correct_answer else R.string.almost_answer),
                    style = MaterialTheme.typography.titleMedium,
                    color = onVivid(),
                    modifier = Modifier.widthIn(max = ReadingContentWidth).fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                )
            }
            GameButton(
                text = stringResource(R.string.next),
                onClick = { viewModel.onIntent(ChoiceIntent.Next) },
                enabled = feedback != null,
                containerColor = studyCardColor(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(max = ReadingContentWidth).fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
            )
        },
    ) {
        val round = state.current
        if (round == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.game_choice),
                    tinted = true,
                )
            }
            return@GameScaffold
        }
        val compactAnswers = currentWindowClass() == WindowClass.EXPANDED && isShortWindow()
        AdaptivePanes(
            first = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.how_translate),
                        style = MaterialTheme.typography.bodySmall,
                        color = onVividMuted(),
                    )
                    Text(
                        text = round.promptUdm,
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center,
                        color = onVivid(),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    if (hasBundledSpeech(round.promptUdm)) {
                        SoundButton(
                            onClick = viewModel::speakPrompt,
                            contentDescription = stringResource(R.string.pronunciation),
                            container = onVivid().copy(alpha = 0.13f),
                            contentColor = onVivid(),
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            },
            second = {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = if (compactAnswers) 2 else 1,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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
                            modifier = Modifier.weight(1f),
                            leadingAligned = true,
                        )
                    }
                }
            },
        )
    }
}
