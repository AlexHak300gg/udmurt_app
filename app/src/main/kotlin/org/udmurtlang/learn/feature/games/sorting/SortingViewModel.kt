package org.udmurtlang.learn.feature.games.sorting

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
import org.udmurtlang.learn.domain.game.SortingThemes
import org.udmurtlang.learn.domain.model.GameRound
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.SubmitGameResultUseCase
import org.udmurtlang.learn.feature.games.FeedbackKind
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameSession
import javax.inject.Inject
import kotlin.random.Random

data class SortingState(
    val round: GameRound.Sorting? = null,
    val placedWordIds: Set<String> = emptySet(),
    val selectedItemIndex: Int? = null,
    val correctCount: Int = 0,
    val feedback: FeedbackKind? = null,
    val locked: Boolean = false,
    val finished: Boolean = false,
) {
    val remaining: List<Int>
        get() {
            val items = round?.items ?: return emptyList()
            return items.indices.filter { items[it].wordId !in placedWordIds }
        }
}

sealed interface SortingIntent {
    data class ItemSelected(val index: Int) : SortingIntent
    data class CategorySelected(val categoryIndex: Int) : SortingIntent
}

@HiltViewModel
class SortingViewModel @Inject constructor(
    private val content: ContentRepository,
    submit: SubmitGameResultUseCase,
    markAnswer: MarkAnswerUseCase,
    feedback: FeedbackPort,
    time: TimeProvider,
) : ViewModel() {

    private val generator = GameRoundGenerator(Random.Default)
    private val session = GameSession(GameType.SORTING, submit, markAnswer, feedback, time)

    private val _state = MutableStateFlow(SortingState())
    val state: StateFlow<SortingState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val allWords = content.allWords()
            val theme = SortingThemes.all.random()
            val round = generator.sorting(allWords, theme)
            _state.update { it.copy(round = round, finished = round == null) }
            if (round == null) finish()
        }
    }

    fun onIntent(intent: SortingIntent) {
        when (intent) {
            is SortingIntent.ItemSelected -> {
                if (_state.value.locked || _state.value.finished) return
                _state.update { it.copy(selectedItemIndex = intent.index) }
            }
            is SortingIntent.CategorySelected -> assign(intent.categoryIndex)
        }
    }

    private fun assign(categoryIndex: Int) {
        val round = _state.value.round ?: return
        val selectedIndex = _state.value.selectedItemIndex ?: return
        if (_state.value.locked || _state.value.finished) return
        val item = round.items.getOrNull(selectedIndex) ?: return
        val isCorrect = item.categoryIndex == categoryIndex
        viewModelScope.launch {
            session.answer(item.wordId, isCorrect)
            if (isCorrect) {
                _state.update {
                    it.copy(
                        placedWordIds = it.placedWordIds + item.wordId,
                        selectedItemIndex = null,
                        feedback = FeedbackKind.CORRECT,
                        correctCount = it.correctCount + 1,
                        locked = true,
                    )
                }
                delay(FEEDBACK_MS)
                val allPlaced = round.items.all { placed -> placed.wordId in _state.value.placedWordIds }
                _state.update { it.copy(feedback = null, locked = false) }
                if (allPlaced) {
                    _state.update { it.copy(finished = true) }
                    finish()
                }
            } else {
                _state.update {
                    it.copy(feedback = FeedbackKind.WRONG, locked = true)
                }
                delay(FEEDBACK_MS)
                _state.update { it.copy(feedback = null, locked = false, selectedItemIndex = null) }
            }
        }
    }

    private fun finish() {
        viewModelScope.launch {
            session.victory()
            val total = _state.value.round?.items?.size ?: 0
            val result = session.finish(total)
            _effects.send(
                GameEffect.Finished(
                    slug = GameType.SORTING.slug,
                    score = result.score,
                    correct = result.correct,
                    total = total,
                ),
            )
        }
    }

    companion object {
        private const val FEEDBACK_MS = 500L
    }
}
