package org.udmurtlang.learn

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.udmurtlang.learn.core.common.ProvideUiLanguage
import org.udmurtlang.learn.core.designsystem.ZecheburTheme
import org.udmurtlang.learn.core.designsystem.AppViewport
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.media.WordSpeaker
import org.udmurtlang.learn.domain.model.ThemeMode
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.model.UiLanguage
import org.udmurtlang.learn.feature.navigation.AppNavGraph
import org.udmurtlang.learn.feature.navigation.Routes
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var wordSpeaker: WordSpeaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val currentSettings = settings
            val darkTheme = when (currentSettings?.themeMode ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val deviceLanguage = LocalConfiguration.current.locales[0]?.language
            val language = currentSettings?.uiLanguage
                ?: UiLanguage.entries.find { it.tag == deviceLanguage }
                ?: UiLanguage.RUSSIAN
            ProvideUiLanguage(language, syncWithAndroid = currentSettings != null) {
                ThemedContent(darkTheme, language)
            }
        }
    }

    @Composable
    private fun ThemedContent(darkTheme: Boolean, language: UiLanguage) {
        ZecheburTheme(darkTheme = darkTheme) {
            val transition = remember { Animatable(1f) }
            var lastLanguage by remember { mutableStateOf(language) }
            LaunchedEffect(language) {
                if (lastLanguage != language) {
                    lastLanguage = language
                    transition.snapTo(0f)
                    transition.animateTo(1f, tween(durationMillis = 240, easing = FastOutSlowInEasing))
                }
            }
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Box(
                    Modifier.fillMaxSize().graphicsLayer {
                        alpha = 0.72f + 0.28f * transition.value
                        translationY = 6.dp.toPx() * (1f - transition.value)
                    },
                ) {
                    AppViewport {
                        val navController = rememberNavController()
                        val entry by navController.currentBackStackEntryAsState()
                        val wide = currentWindowClass().isWide
                        val vividTop = when (entry?.destination?.route) {
                            null, Routes.SPLASH, Routes.MENU, Routes.CARDS, Routes.RESULT, Routes.WORD_OF_DAY -> true
                            Routes.AUTH, Routes.ONBOARDING -> !wide
                            Routes.GAME -> when (entry?.arguments?.getString("slug")) {
                                GameType.CHOICE.slug, GameType.ODD_ONE_OUT.slug,
                                GameType.SORTING.slug, GameType.TRUE_FALSE.slug -> true
                                else -> false
                            }
                            else -> false
                        }
                        val lightBars = MaterialTheme.colorScheme.background.luminance() > 0.18f
                        SideEffect {
                            WindowCompat.getInsetsController(window, window.decorView).apply {
                                isAppearanceLightStatusBars = lightBars && !vividTop
                                isAppearanceLightNavigationBars = lightBars
                            }
                        }
                        AppNavGraph(wordSpeaker, navController)
                    }
                }
            }
        }
    }

    override fun onStop() {
        wordSpeaker.stop()
        super.onStop()
    }
}
