package org.udmurtlang.learn.feature.games.truefalse

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
import org.udmurtlang.learn.core.media.WordSpeaker
import org.udmurtlang.learn.domain.game.GameRoundGenerator
import org.udmurtlang.learn.domain.model.GameRound
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.usecase.GetGameWordsUseCase
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.SubmitGameResultUseCase
import org.udmurtlang.learn.feature.games.FeedbackKind
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameSession
import javax.inject.Inject
import kotlin.random.Random

data class TrueFalseState(
    val rounds: List<GameRound.TrueFalse> = emptyList(),
    val index: Int = 0,
    val correctCount: Int = 0,
    val feedback: FeedbackKind? = null,
    val locked: Boolean = false,
    val finished: Boolean = false,
) {
    val current: GameRound.TrueFalse? get() = rounds.getOrNull(index)
}

sealed interface TrueFalseIntent {
    data class Answer(val clickedTrue: Boolean) : TrueFalseIntent
}

@HiltViewModel
class TrueFalseViewModel @Inject constructor(
    getWords: GetGameWordsUseCase,
    submit: SubmitGameResultUseCase,
    markAnswer: MarkAnswerUseCase,
    feedback: FeedbackPort,
    time: TimeProvider,
    private val speaker: WordSpeaker,
) : ViewModel() {

    private val generator = GameRoundGenerator(Random.Default)
    private val session = GameSession(GameType.TRUE_FALSE, submit, markAnswer, feedback, time)

    private val _state = MutableStateFlow(TrueFalseState())
    val state: StateFlow<TrueFalseState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val words = getWords(GameType.TRUE_FALSE, ROUNDS)
            val rounds = words.map { generator.trueFalse(it, words) }
            _state.update { it.copy(rounds = rounds, finished = rounds.isEmpty()) }
            if (rounds.isEmpty()) finish()
        }
    }

    fun onIntent(intent: TrueFalseIntent) {
        when (intent) {
            is TrueFalseIntent.Answer -> answer(intent.clickedTrue)
        }
    }

    fun speakWord() {
        _state.value.current?.let { speaker.speak(it.udm) }
    }

    private fun answer(clickedTrue: Boolean) {
        val current = _state.value.current ?: return
        if (_state.value.locked || _state.value.finished) return
        val isCorrect = clickedTrue == current.isTrue
        viewModelScope.launch {
            session.answer(current.wordId, isCorrect)
            _state.update {
                it.copy(
                    feedback = if (isCorrect) FeedbackKind.CORRECT else FeedbackKind.WRONG,
                    locked = true,
                    correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                )
            }
            delay(FEEDBACK_MS)
            val next = _state.value.index + 1
            val isLast = next >= _state.value.rounds.size
            _state.update {
                it.copy(index = next, feedback = null, locked = false, finished = isLast)
            }
            if (isLast) finish()
        }
    }

    private fun finish() {
        viewModelScope.launch {
            session.victory()
            val total = _state.value.rounds.size
            val result = session.finish(total)
            _effects.send(
                GameEffect.Finished(
                    slug = GameType.TRUE_FALSE.slug,
                    score = result.score,
                    correct = result.correct,
                    total = total,
                ),
            )
        }
    }

    companion object {
        const val ROUNDS = 8
        private const val FEEDBACK_MS = 650L
    }
}
