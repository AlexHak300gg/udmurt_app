package org.udmurtlang.learn.feature.games

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.game.GameScoring
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.usecase.UnlockAchievementsUseCase
import javax.inject.Inject

data class ResultState(
    val slug: String = "",
    val score: Int = 0,
    val correct: Int = 0,
    val total: Int = 0,
) {
    val stars: Int get() = GameScoring.stars(correct, total)
    val gameType: GameType? get() = GameType.entries.find { it.slug == slug }
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    unlockAchievements: UnlockAchievementsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ResultState(
            slug = savedStateHandle.get<String>("slug").orEmpty(),
            score = savedStateHandle.get<Int>("score") ?: 0,
            correct = savedStateHandle.get<Int>("correct") ?: 0,
            total = savedStateHandle.get<Int>("total") ?: 0,
        ),
    )
    val state: StateFlow<ResultState> = _state.asStateFlow()

    init {
        viewModelScope.launch { unlockAchievements() }
    }
}
