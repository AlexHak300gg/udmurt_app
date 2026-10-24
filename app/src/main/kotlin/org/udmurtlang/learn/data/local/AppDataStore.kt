package org.udmurtlang.learn.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.domain.model.AppSettings
import org.udmurtlang.learn.domain.model.ThemeMode
import org.udmurtlang.learn.domain.model.UiLanguage
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "zechebur_prefs")

@Singleton
class AppDataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val store = context.appDataStore

    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            soundEnabled = prefs[Keys.SOUND] ?: true,
            ttsVolume = prefs[Keys.VOLUME] ?: 1f,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: false,
            geolocationEnabled = prefs[Keys.GEOLOCATION] ?: false,
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            uiLanguage = prefs[Keys.LANGUAGE]?.let { runCatching { UiLanguage.valueOf(it) }.getOrNull() }
                ?: UiLanguage.RUSSIAN,
        )
    }

    val sessionId: Flow<String?> = store.data.map { it[Keys.SESSION] }

    val onboardingDone: Flow<Boolean> = store.data.map { it[Keys.ONBOARDING] ?: false }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = settingsFirst()
        val next = transform(current)
        store.edit { prefs ->
            prefs[Keys.SOUND] = next.soundEnabled
            prefs[Keys.VOLUME] = next.ttsVolume
            prefs[Keys.NOTIFICATIONS] = next.notificationsEnabled
            prefs[Keys.GEOLOCATION] = next.geolocationEnabled
            prefs[Keys.THEME] = next.themeMode.name
            prefs[Keys.LANGUAGE] = next.uiLanguage.name
        }
    }

    suspend fun setSessionId(id: String?) {
        store.edit { prefs ->
            if (id == null) prefs.remove(Keys.SESSION) else prefs[Keys.SESSION] = id
        }
    }

    suspend fun setOnboardingDone() {
        store.edit { it[Keys.ONBOARDING] = true }
    }

    private suspend fun settingsFirst(): AppSettings = settings.first()

    private object Keys {
        val SOUND = booleanPreferencesKey("sound_enabled")
        val VOLUME = floatPreferencesKey("tts_volume")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val GEOLOCATION = booleanPreferencesKey("geolocation_enabled")
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("ui_language")
        val SESSION = stringPreferencesKey("session_account_id")
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
    }
}
