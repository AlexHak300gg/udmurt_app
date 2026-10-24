package org.udmurtlang.learn.feature.games.memory

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
import org.udmurtlang.learn.domain.model.MemorySide
import org.udmurtlang.learn.domain.usecase.GetGameWordsUseCase
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.SubmitGameResultUseCase
import org.udmurtlang.learn.feature.games.FeedbackKind
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameSession
import javax.inject.Inject
import kotlin.random.Random

data class MemoryState(
    val round: GameRound.MemoryCard? = null,
    val matchedWordIds: Set<String> = emptySet(),
    val selectedLeft: MemorySide? = null,
    val selectedRight: MemorySide? = null,
    val wrongWordIds: Set<String> = emptySet(),
    val correctCount: Int = 0,
    val feedback: FeedbackKind? = null,
    val locked: Boolean = false,
    val finished: Boolean = false,
)

sealed interface MemoryIntent {
    data class SelectLeft(val side: MemorySide) : MemoryIntent
    data class SelectRight(val side: MemorySide) : MemoryIntent
}

@HiltViewModel
class MemoryViewModel @Inject constructor(
    getWords: GetGameWordsUseCase,
    submit: SubmitGameResultUseCase,
    markAnswer: MarkAnswerUseCase,
    feedback: FeedbackPort,
    time: TimeProvider,
    private val speaker: WordSpeaker,
) : ViewModel() {

    private val generator = GameRoundGenerator(Random.Default)
    private val session = GameSession(GameType.MEMORY, submit, markAnswer, feedback, time)

    private val _state = MutableStateFlow(MemoryState())
    val state: StateFlow<MemoryState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val words = getWords(GameType.MEMORY, PAIR_COUNT)
            val round = generator.memory(words, PAIR_COUNT)
            _state.update { it.copy(round = round, finished = round == null) }
            if (round == null) finish()
        }
    }

    fun onIntent(intent: MemoryIntent) {
        when (intent) {
            is MemoryIntent.SelectLeft -> selectLeft(intent.side)
            is MemoryIntent.SelectRight -> selectRight(intent.side)
        }
    }

    private fun selectLeft(side: MemorySide) {
        if (_state.value.locked || _state.value.finished) return
        if (side.wordId in _state.value.matchedWordIds) return
        _state.update { it.copy(selectedLeft = side) }
        checkPair()
    }

    private fun selectRight(side: MemorySide) {
        if (_state.value.locked || _state.value.finished) return
        if (side.wordId in _state.value.matchedWordIds) return
        _state.update { it.copy(selectedRight = side) }
        checkPair()
    }

    private fun checkPair() {
        val left = _state.value.selectedLeft ?: return
        val right = _state.value.selectedRight ?: return
        val isMatch = left.wordId == right.wordId
        viewModelScope.launch {
            session.answer(left.wordId, isMatch)
            if (isMatch) {
                speaker.speak(left.text.takeIf { !right.isEmoji } ?: left.text)
                _state.update {
                    it.copy(
                        matchedWordIds = it.matchedWordIds + left.wordId,
                        selectedLeft = null,
                        selectedRight = null,
                        correctCount = it.correctCount + 1,
                        feedback = FeedbackKind.CORRECT,
                        locked = true,
                    )
                }
                delay(FEEDBACK_MS)
                val round = _state.value.round
                val allMatched = round != null &&
                    round.left.all { side -> side.wordId in _state.value.matchedWordIds }
                _state.update { it.copy(feedback = null, locked = false) }
                if (allMatched) {
                    _state.update { it.copy(finished = true) }
                    finish()
                }
            } else {
                _state.update {
                    it.copy(
                        wrongWordIds = setOf(left.wordId, right.wordId),
                        feedback = FeedbackKind.WRONG,
                        locked = true,
                    )
                }
                delay(WRONG_MS)
                _state.update {
                    it.copy(
                        wrongWordIds = emptySet(),
                        selectedLeft = null,
                        selectedRight = null,
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
            val total = _state.value.round?.left?.size ?: 0
            val result = session.finish(total)
            _effects.send(
                GameEffect.Finished(
                    slug = GameType.MEMORY.slug,
                    score = result.score,
                    correct = result.correct,
                    total = total,
                ),
            )
        }
    }

    companion object {
        const val PAIR_COUNT = 4
        private const val FEEDBACK_MS = 450L
        private const val WRONG_MS = 700L
    }
}
