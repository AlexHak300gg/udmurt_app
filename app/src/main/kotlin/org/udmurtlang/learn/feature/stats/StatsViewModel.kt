package org.udmurtlang.learn.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.model.StatsSnapshot
import org.udmurtlang.learn.domain.usecase.GetStatsUseCase
import javax.inject.Inject

data class StatsUiState(
    val snapshot: StatsSnapshot? = null,
    val loading: Boolean = true,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getStats: GetStatsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = StatsUiState(snapshot = getStats(), loading = false)
        }
    }
}
