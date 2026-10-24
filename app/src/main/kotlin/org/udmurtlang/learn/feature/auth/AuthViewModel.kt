package org.udmurtlang.learn.feature.auth

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
import org.udmurtlang.learn.domain.usecase.ContinueAsGuestUseCase
import org.udmurtlang.learn.domain.usecase.LoginUseCase
import org.udmurtlang.learn.domain.usecase.RegisterUseCase
import javax.inject.Inject

data class AuthState(
    val isRegisterMode: Boolean = false,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCloudMode: Boolean = false,
)

sealed interface AuthIntent {
    data class EmailChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data class NameChanged(val value: String) : AuthIntent
    data object ToggleMode : AuthIntent
    data object Submit : AuthIntent
    data object ContinueAsGuest : AuthIntent
    data object ForgotPassword : AuthIntent
}

sealed interface AuthEffect {
    data object Authenticated : AuthEffect
    data class ShowMessage(val text: String) : AuthEffect
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val register: RegisterUseCase,
    private val login: LoginUseCase,
    private val guest: ContinueAsGuestUseCase,
    private val cloudModeProvider: org.udmurtlang.learn.data.supabase.SupabaseProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState(isCloudMode = cloudModeProvider.isReady))
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged -> _state.update { it.copy(email = intent.value, error = null) }
            is AuthIntent.PasswordChanged -> _state.update { it.copy(password = intent.value, error = null) }
            is AuthIntent.NameChanged -> _state.update { it.copy(name = intent.value, error = null) }
            AuthIntent.ToggleMode -> _state.update {
                it.copy(isRegisterMode = !it.isRegisterMode, error = null)
            }
            AuthIntent.Submit -> submit()
            AuthIntent.ContinueAsGuest -> continueAsGuest()
            AuthIntent.ForgotPassword -> forgotPassword()
        }
    }

    private fun submit() {
        val current = _state.value
        val emailError = if (!EMAIL_REGEX.matches(current.email.trim())) "auth_error_email" else null
        val passwordError = if (current.password.length < 6) "auth_error_password" else null
        val nameError = if (current.isRegisterMode && current.name.isBlank()) "auth_error_name" else null
        val firstError = emailError ?: passwordError ?: nameError
        if (firstError != null) {
            _state.update { it.copy(error = firstError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = if (current.isRegisterMode) {
                register(current.email, current.password, current.name)
            } else {
                login(current.email, current.password)
            }
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    _effects.send(AuthEffect.Authenticated)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: "Что-то пошло не так")
                    }
                },
            )
        }
    }

    private fun continueAsGuest() {
        viewModelScope.launch {
            guest()
            _effects.send(AuthEffect.Authenticated)
        }
    }

    private fun forgotPassword() {
        viewModelScope.launch {
            val message = if (_state.value.isCloudMode) "auth_forgot_cloud" else "auth_forgot_local"
            _effects.send(AuthEffect.ShowMessage(message))
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
