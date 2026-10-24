package org.udmurtlang.learn.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.udmurtlang.learn.core.common.UiLanguageController
import org.udmurtlang.learn.domain.model.AppSettings
import org.udmurtlang.learn.domain.model.ThemeMode
import org.udmurtlang.learn.domain.model.UiLanguage
import org.udmurtlang.learn.domain.notifications.NotificationScheduler
import org.udmurtlang.learn.domain.usecase.ObserveSettingsUseCase
import org.udmurtlang.learn.domain.usecase.UpdateSettingsUseCase
import javax.inject.Inject

data class SettingsUiState(
    val notificationsDenied: Boolean = false,
    val geoDenied: Boolean = false,
)

sealed interface SettingsIntent {
    data class SoundToggled(val enabled: Boolean) : SettingsIntent
    data class VolumeChanged(val value: Float) : SettingsIntent
    data class NotificationsToggled(val enabled: Boolean) : SettingsIntent
    data class GeolocationToggled(val enabled: Boolean) : SettingsIntent
    data class ThemeSelected(val mode: ThemeMode) : SettingsIntent
    data class LanguageSelected(val language: UiLanguage) : SettingsIntent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val uiLanguageController: UiLanguageController,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SoundToggled -> update { it.copy(soundEnabled = intent.enabled) }
            is SettingsIntent.VolumeChanged -> update { it.copy(ttsVolume = intent.value.coerceIn(0f, 1f)) }
            is SettingsIntent.NotificationsToggled -> {
                update { it.copy(notificationsEnabled = intent.enabled) }
                notificationScheduler.sync(intent.enabled)
                if (intent.enabled) _uiState.update { it.copy(notificationsDenied = false) }
            }
            is SettingsIntent.GeolocationToggled -> {
                update { it.copy(geolocationEnabled = intent.enabled) }
                _uiState.update { it.copy(geoDenied = !intent.enabled) }
            }
            is SettingsIntent.ThemeSelected -> update { it.copy(themeMode = intent.mode) }
            is SettingsIntent.LanguageSelected -> {
                update { it.copy(uiLanguage = intent.language) }
                uiLanguageController.apply(intent.language)
            }
        }
    }

    fun markNotificationsDenied() {
        _uiState.update { it.copy(notificationsDenied = true) }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { updateSettings(transform) }
    }
}
