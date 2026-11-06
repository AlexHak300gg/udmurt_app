package org.udmurtlang.learn.feature.learning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.udmurtlang.learn.core.media.WordSpeaker
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.domain.repository.ProgressRepository
import org.udmurtlang.learn.domain.usecase.GetTopicUseCase
import org.udmurtlang.learn.domain.usecase.GetTopicWordsUseCase
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.MarkWordKnownUseCase
import org.udmurtlang.learn.domain.usecase.UnlockAchievementsUseCase
import javax.inject.Inject

data class CardsState(
    val topicId: String = "",
    val topic: Topic? = null,
    val words: List<Word> = emptyList(),
    val statuses: Map<String, ProgressStatus> = emptyMap(),
    val index: Int = 0,
    val finished: Boolean = false,
    val answering: Boolean = false,
) {
    val current: Word? get() = words.getOrNull(index)
    val knownCount: Int get() = words.count { statuses[it.id] == ProgressStatus.KNOWN }
}

sealed interface CardsIntent {
    data object Know : CardsIntent
    data object Repeat : CardsIntent
    data class Speak(val word: Word) : CardsIntent
}

@HiltViewModel
class WordCardsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWords: GetTopicWordsUseCase,
    private val getTopic: GetTopicUseCase,
    private val progress: ProgressRepository,
    private val markAnswer: MarkAnswerUseCase,
    private val markKnown: MarkWordKnownUseCase,
    private val unlockAchievements: UnlockAchievementsUseCase,
    private val speaker: WordSpeaker,
) : ViewModel() {

    private val topicId: String = checkNotNull(savedStateHandle["topicId"])

    private val _state = MutableStateFlow(CardsState(topicId = topicId))
    val state: StateFlow<CardsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val words = getWords(topicId)
            _state.update { it.copy(words = words) }
        }
        viewModelScope.launch {
            _state.update { it.copy(topic = getTopic(topicId)) }
        }
        viewModelScope.launch {
            progress.observeTopicStatuses(topicId).collect { statuses ->
                _state.update { it.copy(statuses = statuses) }
            }
        }
    }

    fun onIntent(intent: CardsIntent) {
        when (intent) {
            is CardsIntent.Speak -> speaker.speak(intent.word.udm)
            CardsIntent.Know -> answer(correct = true)
            CardsIntent.Repeat -> answer(correct = false)
        }
    }

    private fun answer(correct: Boolean) {
        val state = _state.value
        val current = state.current ?: return
        if (state.answering || state.finished) return
        _state.update { it.copy(answering = true) }
        viewModelScope.launch {
            try {
                if (correct) {
                    markKnown(current.id)
                    unlockAchievements()
                } else {
                    markAnswer(current.id, correct = false)
                }
                val next = state.index + 1
                _state.update { it.copy(index = next, finished = next >= it.words.size) }
            } finally {
                _state.update { it.copy(answering = false) }
            }
        }
    }
}
