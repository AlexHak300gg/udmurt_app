package org.udmurtlang.learn.feature.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.model.DailyActivity
import org.udmurtlang.learn.domain.model.Profile
import org.udmurtlang.learn.domain.model.Streak
import org.udmurtlang.learn.domain.model.WordOfDay
import org.udmurtlang.learn.domain.repository.AccountRepository
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.usecase.GetWordOfDayUseCase
import org.udmurtlang.learn.domain.usecase.UnlockAchievementsUseCase
import javax.inject.Inject

data class MenuState(
    val profile: Profile? = null,
    val streak: Streak = Streak(),
    val wordOfDay: WordOfDay? = null,
    val loading: Boolean = true,
    val monthActivity: List<DailyActivity> = emptyList(),
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    account: AccountRepository,
    private val activity: ActivityRepository,
    private val getWordOfDay: GetWordOfDayUseCase,
    private val unlockAchievements: UnlockAchievementsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MenuState())
    val state: StateFlow<MenuState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            account.observeProfile().collect { profile ->
                _state.update { it.copy(profile = profile, loading = false) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val streak = activity.streak()
            val wordOfDay = getWordOfDay()
            _state.update { it.copy(streak = streak, wordOfDay = wordOfDay) }
            unlockAchievements()
        }
    }

    fun loadMonthActivity() {
        viewModelScope.launch {
            val days = activity.daily(DAYS_FETCHED)
            _state.update { it.copy(monthActivity = days) }
        }
    }

    private companion object {
        const val DAYS_FETCHED = 31
    }
}
