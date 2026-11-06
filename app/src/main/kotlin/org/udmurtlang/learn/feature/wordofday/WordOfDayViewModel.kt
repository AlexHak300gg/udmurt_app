package org.udmurtlang.learn.feature.wordofday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.udmurtlang.learn.core.media.WordSpeaker
import org.udmurtlang.learn.domain.model.WordOfDay
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.domain.usecase.GetTopicWordsUseCase
import org.udmurtlang.learn.domain.usecase.GetWordOfDayUseCase
import javax.inject.Inject

data class WordOfDayState(
    val wod: WordOfDay? = null,
    val related: List<Word> = emptyList(),
)

@HiltViewModel
class WordOfDayViewModel @Inject constructor(
    getWordOfDay: GetWordOfDayUseCase,
    getTopicWords: GetTopicWordsUseCase,
    private val speaker: WordSpeaker,
) : ViewModel() {

    private val _state = MutableStateFlow(WordOfDayState())
    val state: StateFlow<WordOfDayState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val day = getWordOfDay()
            _state.update { it.copy(wod = day) }
            val related = getTopicWords(day.word.topicId).filterNot { it.id == day.word.id }.take(3)
            _state.update { it.copy(related = related) }
        }
    }

    fun speak() {
        _state.value.wod?.let { speaker.speak(it.word.udm) }
    }

    fun speakRelated(word: Word) = speaker.speak(word.udm)
}
