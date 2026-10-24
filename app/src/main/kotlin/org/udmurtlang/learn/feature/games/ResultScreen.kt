package org.udmurtlang.learn.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.ZcGold
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.components.ConfettiOverlay
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.GhostButton
import org.udmurtlang.learn.core.designsystem.components.StatTile
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.core.designsystem.vividScreen

@Composable
fun ResultScreen(
    onPlayAgain: (String) -> Unit,
    onToMenu: () -> Unit,
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
        modifier = Modifier
            .fillMaxSize()
            .background(vividScreen(ZcOrange))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { index ->
                Text(
                    text = "★",
                    fontSize = 34.sp,
                    color = if (index < stars) ZcGold else onVividMuted(),
                )
            }
        }
        Text(
            text = stringResource(R.string.game_finished),
            style = MaterialTheme.typography.displaySmall,
            color = onVivid(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
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
                emoji = "🏆",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = "+${state.correct}",
                caption = wordsLabel,
                emoji = "⭐",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = "$stars",
                caption = starsLabel,
                emoji = "🎯",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 22.dp),
        )
        OrnamentBand(
            color = onVivid().copy(alpha = 0.85f),
            modifier = Modifier.padding(bottom = 22.dp),
        )
    }
    ConfettiOverlay(active = state.stars >= 2)
}
