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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.udmurtlang.learn.core.designsystem.ZecheburTheme
import org.udmurtlang.learn.core.media.WordSpeaker
import org.udmurtlang.learn.domain.model.ThemeMode
import org.udmurtlang.learn.feature.navigation.AppNavGraph
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
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            ZecheburTheme(darkTheme = darkTheme) {
                val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.toLanguageTag()
                val transition = remember { Animatable(1f) }
                var lastLocale by remember { mutableStateOf(locale) }
                LaunchedEffect(locale) {
                    if (lastLocale != locale) {
                        lastLocale = locale
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
                        AppNavGraph(wordSpeaker)
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
