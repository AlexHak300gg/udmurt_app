package org.udmurtlang.learn.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.udmurtlang.learn.domain.model.UiLanguage
import javax.inject.Inject

/** Применение языка интерфейса к ресурсам всего приложения. */
class UiLanguageController @Inject constructor() {

    fun apply(language: UiLanguage) {
        val tag = when (language) {
            UiLanguage.RUSSIAN -> LOCALE_RU
            UiLanguage.UDMURT -> LOCALE_UDM
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    private companion object {
        const val LOCALE_RU = "ru"
        const val LOCALE_UDM = "udm"
    }
}
