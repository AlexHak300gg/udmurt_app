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
import org.udmurtlang.learn.domain.usecase.GetWordOfDayUseCase
import org.udmurtlang.learn.domain.usecase.MarkWordKnownUseCase
import javax.inject.Inject

data class WordOfDayState(
    val wod: WordOfDay? = null,
    val marked: Boolean = false,
)

@HiltViewModel
class WordOfDayViewModel @Inject constructor(
    getWordOfDay: GetWordOfDayUseCase,
    private val markKnown: MarkWordKnownUseCase,
    private val speaker: WordSpeaker,
) : ViewModel() {

    private val _state = MutableStateFlow(WordOfDayState())
    val state: StateFlow<WordOfDayState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(wod = getWordOfDay()) }
        }
    }

    fun speak() {
        _state.value.wod?.let { speaker.speak(it.word.udm) }
    }

    fun markKnown() {
        val wod = _state.value.wod ?: return
        viewModelScope.launch {
            markKnown(wod.word.id)
            speaker.speak(wod.word.udm)
            _state.update { it.copy(marked = true) }
        }
    }
}
