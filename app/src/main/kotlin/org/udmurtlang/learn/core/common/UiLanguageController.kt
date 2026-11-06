package org.udmurtlang.learn.core.common

import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.os.LocaleListCompat
import org.udmurtlang.learn.domain.model.UiLanguage
import java.util.Locale

@Composable
fun ProvideUiLanguage(language: UiLanguage, syncWithAndroid: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val localizedConfiguration = remember(configuration, language) {
        Configuration(configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag(language.tag)))
        }
    }
    val resources = remember(context, localizedConfiguration) {
        context.createConfigurationContext(localizedConfiguration).resources
    }
    LaunchedEffect(language, syncWithAndroid) {
        if (syncWithAndroid && AppCompatDelegate.getApplicationLocales().toLanguageTags() != language.tag) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }
    }
    CompositionLocalProvider(
        LocalConfiguration provides localizedConfiguration,
        LocalResources provides resources,
        content = content,
    )
}
