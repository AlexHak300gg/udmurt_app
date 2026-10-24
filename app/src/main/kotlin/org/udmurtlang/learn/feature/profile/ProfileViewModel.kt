package org.udmurtlang.learn.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.udmurtlang.learn.domain.model.Achievement
import org.udmurtlang.learn.domain.model.Profile
import org.udmurtlang.learn.domain.model.StatsSnapshot
import org.udmurtlang.learn.domain.repository.AccountRepository
import org.udmurtlang.learn.domain.usecase.GetStatsUseCase
import org.udmurtlang.learn.domain.usecase.ObserveAchievementsUseCase
import javax.inject.Inject

data class ProfileState(
    val profile: Profile? = null,
    val editingName: String = "",
    val isEditing: Boolean = false,
    val achievements: List<Achievement> = emptyList(),
    val stats: StatsSnapshot? = null,
    val passwordDialog: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val passwordError: String? = null,
    val deleteDialog: Boolean = false,
    val notice: String? = null,
)

sealed interface ProfileIntent {
    data object EditName : ProfileIntent
    data class NameChanged(val value: String) : ProfileIntent
    data object SaveName : ProfileIntent
    data object CancelEdit : ProfileIntent
    data class SelectAvatar(val avatar: String) : ProfileIntent
    data object EditPassword : ProfileIntent
    data class OldPasswordChanged(val value: String) : ProfileIntent
    data class NewPasswordChanged(val value: String) : ProfileIntent
    data object SavePassword : ProfileIntent
    data object CancelPassword : ProfileIntent
    data object DeleteAccount : ProfileIntent
    data object ConfirmDelete : ProfileIntent
    data object CancelDelete : ProfileIntent
    data object Logout : ProfileIntent
    data object DismissNotice : ProfileIntent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val account: AccountRepository,
    getStats: GetStatsUseCase,
    observeAchievements: ObserveAchievementsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            account.observeProfile().collect { profile ->
                _state.update {
                    it.copy(
                        profile = profile,
                        editingName = if (it.isEditing) it.editingName else profile?.displayName.orEmpty(),
                    )
                }
            }
        }
        viewModelScope.launch {
            _state.update { it.copy(stats = getStats()) }
        }
        viewModelScope.launch {
            observeAchievements().collect { unlocked ->
                _state.update { it.copy(achievements = unlocked) }
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.EditName -> _state.update {
                it.copy(isEditing = true, editingName = it.profile?.displayName.orEmpty())
            }
            is ProfileIntent.NameChanged -> _state.update { it.copy(editingName = intent.value) }
            ProfileIntent.SaveName -> {
                val name = _state.value.editingName.trim()
                if (name.isBlank()) return
                viewModelScope.launch {
                    account.updateName(name)
                    _state.update { it.copy(isEditing = false, notice = null) }
                }
            }
            ProfileIntent.CancelEdit -> _state.update { it.copy(isEditing = false) }
            is ProfileIntent.SelectAvatar -> viewModelScope.launch {
                account.updateAvatar(intent.avatar)
            }
            ProfileIntent.EditPassword -> _state.update {
                it.copy(passwordDialog = true, oldPassword = "", newPassword = "", passwordError = null)
            }
            is ProfileIntent.OldPasswordChanged -> _state.update {
                it.copy(oldPassword = intent.value, passwordError = null)
            }
            is ProfileIntent.NewPasswordChanged -> _state.update {
                it.copy(newPassword = intent.value, passwordError = null)
            }
            ProfileIntent.SavePassword -> changePassword()
            ProfileIntent.CancelPassword -> _state.update {
                it.copy(passwordDialog = false, passwordError = null)
            }
            ProfileIntent.DeleteAccount -> _state.update { it.copy(deleteDialog = true) }
            ProfileIntent.CancelDelete -> _state.update { it.copy(deleteDialog = false) }
            ProfileIntent.ConfirmDelete -> viewModelScope.launch {
                account.deleteAccount()
                _state.update { it.copy(deleteDialog = false, notice = "deleted") }
            }
            ProfileIntent.Logout -> viewModelScope.launch {
                account.logout()
                _state.update { it.copy(notice = "logged_out") }
            }
            ProfileIntent.DismissNotice -> _state.update { it.copy(notice = null) }
        }
    }

    private fun changePassword() {
        val current = _state.value
        if (current.oldPassword.isBlank()) {
            _state.update { it.copy(passwordError = KEY_OLD_EMPTY) }
            return
        }
        if (current.newPassword.length < MIN_PASSWORD) {
            _state.update { it.copy(passwordError = KEY_NEW_SHORT) }
            return
        }
        viewModelScope.launch {
            account.changePassword(current.oldPassword, current.newPassword)
                .fold(
                    onSuccess = { _state.update { it.copy(passwordDialog = false, passwordError = null) } },
                    onFailure = { error -> _state.update { it.copy(passwordError = error.message) } },
                )
        }
    }

    private companion object {
        const val MIN_PASSWORD = 6
        const val KEY_OLD_EMPTY = "profile_error_old"
        const val KEY_NEW_SHORT = "auth_error_password"
    }
}
