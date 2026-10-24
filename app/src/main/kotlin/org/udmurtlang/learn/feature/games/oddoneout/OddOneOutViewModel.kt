package org.udmurtlang.learn.feature.games.oddoneout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.core.media.FeedbackPort
import org.udmurtlang.learn.domain.game.GameRoundGenerator
import org.udmurtlang.learn.domain.model.GameRound
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.usecase.GetWordsGroupedByTopicUseCase
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.SubmitGameResultUseCase
import org.udmurtlang.learn.feature.games.FeedbackKind
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameSession
import javax.inject.Inject
import kotlin.random.Random

data class OddOneOutState(
    val rounds: List<GameRound.OddOneOut> = emptyList(),
    val index: Int = 0,
    val correctCount: Int = 0,
    val feedback: FeedbackKind? = null,
    val selected: Int? = null,
    val locked: Boolean = false,
) {
    val current: GameRound.OddOneOut? get() = rounds.getOrNull(index)
    val finished: Boolean get() = rounds.isNotEmpty() && index >= rounds.size
}

sealed interface OddOneOutIntent {
    data class Select(val wordIndex: Int) : OddOneOutIntent
}

@HiltViewModel
class OddOneOutViewModel @Inject constructor(
    getWordsGrouped: GetWordsGroupedByTopicUseCase,
    submit: SubmitGameResultUseCase,
    markAnswer: MarkAnswerUseCase,
    feedback: FeedbackPort,
    time: TimeProvider,
) : ViewModel() {

    private val generator = GameRoundGenerator(Random.Default)
    private val session = GameSession(GameType.ODD_ONE_OUT, submit, markAnswer, feedback, time)

    private val _state = MutableStateFlow(OddOneOutState())
    val state: StateFlow<OddOneOutState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val grouped = getWordsGrouped()
            val rounds = mutableListOf<GameRound.OddOneOut>()
            var attempts = 0
            while (rounds.size < ROUNDS && attempts < 80) {
                val round = generator.oddOneOut(grouped)
                if (round != null && rounds.none { it.oddWordId == round.oddWordId }) {
                    rounds += round
                }
                attempts++
            }
            _state.update { it.copy(rounds = rounds) }
        }
    }

    fun onIntent(intent: OddOneOutIntent) {
        when (intent) {
            is OddOneOutIntent.Select -> select(intent.wordIndex)
        }
    }

    private fun select(wordIndex: Int) {
        val current = _state.value.current ?: return
        if (_state.value.locked) return
        val isCorrect = wordIndex == current.oddIndex
        viewModelScope.launch {
            session.answer(current.oddWordId, isCorrect)
            _state.update {
                it.copy(
                    selected = wordIndex,
                    feedback = if (isCorrect) FeedbackKind.CORRECT else FeedbackKind.WRONG,
                    locked = true,
                    correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                )
            }
            delay(FEEDBACK_MS)
            val next = _state.value.index + 1
            _state.update { it.copy(index = next, selected = null, feedback = null, locked = false) }
            if (_state.value.finished) finish()
        }
    }

    private fun finish() {
        viewModelScope.launch {
            session.victory()
            val total = _state.value.rounds.size
            val result = session.finish(total)
            _effects.send(
                GameEffect.Finished(
                    slug = GameType.ODD_ONE_OUT.slug,
                    score = result.score,
                    correct = result.correct,
                    total = total,
                ),
            )
        }
    }

    companion object {
        const val ROUNDS = 8
        private const val FEEDBACK_MS = 700L
    }
}
