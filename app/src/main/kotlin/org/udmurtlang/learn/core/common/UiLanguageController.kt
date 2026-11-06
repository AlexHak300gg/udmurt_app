package org.udmurtlang.learn.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.udmurtlang.learn.domain.model.UiLanguage
import javax.inject.Inject

/** Применение языка интерфейса к ресурсам всего приложения. */
class UiLanguageController @Inject constructor() {

    fun apply(language: UiLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}
