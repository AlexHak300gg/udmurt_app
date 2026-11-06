package org.udmurtlang.learn.feature.games.builder

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

data class BuilderState(
    val rounds: List<GameRound.Builder> = emptyList(),
    val index: Int = 0,
    val usedLetterIndexes: List<Int> = emptyList(),
    val built: List<String> = emptyList(),
    val correctCount: Int = 0,
    val feedback: FeedbackKind? = null,
    val locked: Boolean = false,
    val finished: Boolean = false,
) {
    val current: GameRound.Builder? get() = rounds.getOrNull(index)
    val letters: List<String> get() = current?.letters ?: emptyList()
}

sealed interface BuilderIntent {
    data class LetterTapped(val letterIndex: Int) : BuilderIntent
    data object Check : BuilderIntent
    data object Undo : BuilderIntent
}

@HiltViewModel
class BuilderViewModel @Inject constructor(
    getWords: GetGameWordsUseCase,
    submit: SubmitGameResultUseCase,
    markAnswer: MarkAnswerUseCase,
    feedback: FeedbackPort,
    time: TimeProvider,
    private val speaker: WordSpeaker,
) : ViewModel() {

    private val generator = GameRoundGenerator(Random.Default)
    private val session = GameSession(GameType.WORD_BUILDER, submit, markAnswer, feedback, time)

    private val _state = MutableStateFlow(BuilderState())
    val state: StateFlow<BuilderState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val words = getWords(GameType.WORD_BUILDER, ROUNDS)
            val rounds = words.mapNotNull { generator.builder(it) }
            _state.update { it.copy(rounds = rounds, finished = rounds.isEmpty()) }
            if (rounds.isEmpty()) finish()
        }
    }

    fun onIntent(intent: BuilderIntent) {
        when (intent) {
            is BuilderIntent.LetterTapped -> tapLetter(intent.letterIndex)
            BuilderIntent.Check -> checkBuilt()
            BuilderIntent.Undo -> undo()
        }
    }

    fun speakWord() {
        _state.value.current?.let { speaker.speak(it.udm) }
    }

    private fun tapLetter(letterIndex: Int) {
        val state = _state.value
        val round = state.current ?: return
        if (state.locked || state.finished || letterIndex in state.usedLetterIndexes) return
        session.click()
        val letter = round.letters.getOrNull(letterIndex) ?: return
        _state.update {
            it.copy(
                built = it.built + letter,
                usedLetterIndexes = it.usedLetterIndexes + letterIndex,
            )
        }
    }

    private fun checkBuilt() {
        val state = _state.value
        if (state.current == null) return
        if (state.locked || state.finished || state.built.isEmpty()) return
        check(state.built.joinToString(""))
    }

    private fun undo() {
        val state = _state.value
        if (state.locked || state.finished || state.built.isEmpty()) return
        _state.update {
            it.copy(
                built = it.built.dropLast(1),
                usedLetterIndexes = it.usedLetterIndexes.dropLast(1),
            )
        }
    }

    private fun check(answer: String) {
        val round = _state.value.current ?: return
        val isCorrect = answer == round.udm.lowercase().filter(Char::isLetter)
        _state.update { it.copy(locked = true) }
        viewModelScope.launch {
            session.answer(round.wordId, isCorrect)
            _state.update {
                it.copy(
                    feedback = if (isCorrect) FeedbackKind.CORRECT else FeedbackKind.WRONG,
                    correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                )
            }
            delay(if (isCorrect) CORRECT_MS else WRONG_MS)
            if (isCorrect) {
                val next = _state.value.index + 1
                val isLast = next >= _state.value.rounds.size
                _state.update {
                    it.copy(
                        index = next,
                        built = emptyList(),
                        usedLetterIndexes = emptyList(),
                        feedback = null,
                        locked = false,
                        finished = isLast,
                    )
                }
                if (isLast) finish()
            } else {
                _state.update {
                    it.copy(
                        built = emptyList(),
                        usedLetterIndexes = emptyList(),
                        feedback = null,
                        locked = false,
                    )
                }
            }
        }
    }

    private fun finish() {
        viewModelScope.launch {
            session.victory()
            val total = _state.value.rounds.size
            val result = session.finish(total)
            _effects.send(
                GameEffect.Finished(
                    slug = GameType.WORD_BUILDER.slug,
                    score = result.score,
                    correct = result.correct,
                    total = total,
                ),
            )
        }
    }

    companion object {
        const val ROUNDS = 8
        private const val CORRECT_MS = 700L
        private const val WRONG_MS = 800L
    }
}
