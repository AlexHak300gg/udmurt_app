package org.udmurtlang.learn.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.StarGlyph
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.components.ConfettiOverlay
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.GhostButton
import org.udmurtlang.learn.core.designsystem.components.StatTile
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.core.designsystem.vividScreen
import org.udmurtlang.learn.core.designsystem.studyCardColor

@Composable
fun ResultScreen(
    onPlayAgain: (String) -> Unit,
    onToMenu: () -> Unit,
    onBack: () -> Unit = onToMenu,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stars = state.stars.coerceIn(0, 3)
    val mood = when (stars) {
        3 -> R.string.result_mood_3
        2 -> R.string.result_mood_2
        1 -> R.string.result_mood_1
        else -> R.string.result_mood_0
    }
    val scoreLabel = stringResource(R.string.result_score)
    val wordsLabel = stringResource(R.string.result_words)
    val starsLabel = stringResource(R.string.result_stars)

    Column(
        modifier = Modifier.fillMaxSize().background(vividScreen(ZcOrange)).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = "",
            onBack = onBack,
            compact = true,
            tinted = true,
            modifier = Modifier.widthIn(max = WideContentWidth),
        )
        val wide = currentWindowClass().isWide
        ScrollableContent(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            maxWidth = WideContentWidth,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            centerVertically = wide,
            fillViewport = true,
        ) {
            val summary: @Composable () -> Unit = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(3) { index ->
                            StarGlyph(
                                size = 40.dp,
                                color = onVivid().copy(alpha = if (index < stars) 1f else 0.35f),
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.game_finished),
                        style = MaterialTheme.typography.displaySmall,
                        color = onVivid(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        text = stringResource(mood),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onVividMuted(),
                        modifier = Modifier.padding(top = 4.dp, bottom = 22.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatTile(
                            value = "${state.score}",
                            caption = scoreLabel,
                            container = studyCardColor(),
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = "+${state.correct}",
                            caption = wordsLabel,
                            container = studyCardColor(),
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = "$stars",
                            caption = starsLabel,
                            container = studyCardColor(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            val actions: @Composable () -> Unit = {
                Column {
                    // Ряд фирменных орнаментов над кнопками, как в макете
                    if (!isShortWindow()) OrnamentBand(
                        color = onVivid(),
                        height = 64.dp,
                        diamondSize = 56.dp,
                        modifier = Modifier.padding(top = 28.dp, bottom = 20.dp),
                    )
                    GameButton(
                        text = stringResource(R.string.play_again),
                        onClick = { state.slug.takeIf { it.isNotBlank() }?.let(onPlayAgain) },
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                    )
                    GhostButton(
                        text = stringResource(R.string.to_menu),
                        onClick = onToMenu,
                        containerColor = Color.Transparent,
                        contentColor = onVivid(),
                        borderColor = onVivid(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                    )
                }
            }
            if (wide) {
                AdaptivePanes(first = summary, second = actions)
            } else {
                summary()
                Spacer(Modifier.weight(1f))
                actions()
            }
        }
    }
    ConfettiOverlay(active = state.stars >= 2)
}
