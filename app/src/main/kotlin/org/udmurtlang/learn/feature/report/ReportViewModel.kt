package org.udmurtlang.learn.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.model.ChildReport
import org.udmurtlang.learn.domain.repository.AccountRepository
import org.udmurtlang.learn.domain.usecase.BuildChildReportUseCase
import javax.inject.Inject

data class ReportUiState(
    val report: ChildReport? = null,
    val loading: Boolean = true,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val buildReport: BuildChildReportUseCase,
    private val account: AccountRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = account.observeProfile().first()
            val name = profile?.displayName.orEmpty().ifBlank { "Ребёнок" }
            _state.value = ReportUiState(report = buildReport(name), loading = false)
        }
    }
}
