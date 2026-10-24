package org.udmurtlang.learn.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class UiLanguage(val tag: String) { RUSSIAN("ru"), UDMURT("udm") }

data class Profile(
    val id: String,
    val email: String,
    val displayName: String,
    val avatar: String,
    val isGuest: Boolean,
)

data class AppSettings(
    val soundEnabled: Boolean = true,
    val ttsVolume: Float = 1f,
    val notificationsEnabled: Boolean = false,
    val geolocationEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val uiLanguage: UiLanguage = UiLanguage.RUSSIAN,
)
