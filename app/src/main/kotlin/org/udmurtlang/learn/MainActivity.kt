package org.udmurtlang.learn

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
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
                AppNavGraph()
            }
        }
    }

    override fun onStop() {
        wordSpeaker.stop()
        super.onStop()
    }
}
