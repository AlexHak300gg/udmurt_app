package org.udmurtlang.learn.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.udmurtlang.learn.domain.model.StatsSnapshot
import org.udmurtlang.learn.domain.repository.AccountRepository
import org.udmurtlang.learn.domain.usecase.ObserveStatsUseCase
import javax.inject.Inject

data class StatsUiState(
    val snapshot: StatsSnapshot? = null,
    val displayName: String = "",
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    observeStats: ObserveStatsUseCase,
    account: AccountRepository,
) : ViewModel() {

    val state: StateFlow<StatsUiState> = combine(observeStats(), account.observeProfile()) { stats, profile ->
        StatsUiState(snapshot = stats, displayName = profile?.displayName.orEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
