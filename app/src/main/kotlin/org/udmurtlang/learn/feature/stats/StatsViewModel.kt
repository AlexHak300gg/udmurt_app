package org.udmurtlang.learn.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.model.StatsSnapshot
import org.udmurtlang.learn.domain.repository.AccountRepository
import org.udmurtlang.learn.domain.usecase.GetStatsUseCase
import javax.inject.Inject

data class StatsUiState(
    val snapshot: StatsSnapshot? = null,
    val displayName: String = "",
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getStats: GetStatsUseCase,
    private val account: AccountRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val displayName = account.observeProfile().first()?.displayName.orEmpty()
            _state.value = StatsUiState(snapshot = getStats(), displayName = displayName)
        }
    }
}
