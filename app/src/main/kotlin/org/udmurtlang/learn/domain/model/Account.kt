package org.udmurtlang.learn.domain.model

import java.time.LocalTime

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class UiLanguage(val tag: String) { RUSSIAN("ru"), UDMURT("udm") }

const val MAX_DISPLAY_NAME_LENGTH = 32
const val DEFAULT_GUEST_NAME = "Гость"
val DefaultWordOfDayTime: LocalTime = LocalTime.of(9, 0)

fun String.limitDisplayName(): String =
    if (codePointCount(0, length) <= MAX_DISPLAY_NAME_LENGTH) this
    else substring(0, offsetByCodePoints(0, MAX_DISPLAY_NAME_LENGTH))

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
    val wordOfDayTime: LocalTime = DefaultWordOfDayTime,
)
