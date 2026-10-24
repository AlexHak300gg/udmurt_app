package org.udmurtlang.learn.feature.learning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
) {
    val current: Word? get() = words.getOrNull(index)
    val knownCount: Int get() = words.count { statuses[it.id] == ProgressStatus.KNOWN }
}

sealed interface CardsIntent {
    data object Know : CardsIntent
    data object Repeat : CardsIntent
    data class Speak(val word: Word) : CardsIntent
}

sealed interface CardsEffect {
    data object Celebrate : CardsEffect
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

    private val _effects = Channel<CardsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

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
        val current = _state.value.current ?: return
        viewModelScope.launch {
            if (correct) {
                markKnown(current.id)
                speaker.speak(current.udm)
                unlockAchievements()
            } else {
                markAnswer(current.id, correct = false)
            }
            val next = _state.value.index + 1
            if (next >= _state.value.words.size) {
                _state.update { it.copy(finished = true) }
                _effects.send(CardsEffect.Celebrate)
            } else {
                _state.update { it.copy(index = next) }
            }
        }
    }
}
