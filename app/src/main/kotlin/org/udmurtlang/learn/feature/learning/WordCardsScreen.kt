package org.udmurtlang.learn.feature.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.chromeOnVivid
import org.udmurtlang.learn.core.designsystem.chromeOnVividText
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.vividScreen
import org.udmurtlang.learn.core.designsystem.components.ConfettiOverlay
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.WordCard
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun WordCardsScreen(
    onBack: () -> Unit,
    viewModel: WordCardsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { state.words.size.coerceAtLeast(1) }

    LaunchedEffect(state.index) {
        if (state.index < state.words.size) {
            pagerState.animateScrollToPage(state.index)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CardsEffect.Celebrate -> Unit
            }
        }
    }

    val bg = vividScreen(ZcTeal)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        if (!LocalBrand.current.isDark) {
            StarPattern(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
                color = ZcWhite,
                alpha = 0.08f,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            ZcHeader(
                title = state.topic?.titleRu
                    ?: stringResource(R.string.topics_title),
                caption = state.topic?.titleUdm ?: stringResource(R.string.pronunciation),
                onBack = onBack,
                tinted = true,
                trailing = {
                    Text(
                        text = "${state.knownCount}/${state.words.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = onVivid(),
                    )
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    state.finished -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text("🎉", fontSize = MaterialTheme.typography.displayLarge.fontSize)
                            Text(
                                text = stringResource(R.string.topic_complete),
                                style = MaterialTheme.typography.headlineMedium,
                                color = onVivid(),
                                textAlign = TextAlign.Center,
                            )
                            GameButton(
                                text = stringResource(R.string.back),
                                onClick = onBack,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 24.dp),
                            )
                        }
                        ConfettiOverlay(active = true)
                    }
                    state.words.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = stringResource(R.string.loading),
                                hint = stringResource(R.string.cards_hint),
                            )
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = pagerState,
                                userScrollEnabled = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            ) { page ->
                                val word = state.words[page] ?: return@HorizontalPager
                                WordCard(
                                    word = word,
                                    onSpeak = { viewModel.onIntent(CardsIntent.Speak(word)) },
                                    speakLabel = stringResource(R.string.pronunciation),
                                    translationLabel = stringResource(R.string.word_translation),
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val word = state.current
                                val chrome = chromeOnVivid()
                                val chromeOn = chromeOnVividText()
                                GameButton(
                                    text = stringResource(R.string.repeat),
                                    containerColor = chrome,
                                    contentColor = chromeOn,
                                    enabled = word != null,
                                    onClick = { word?.let { viewModel.onIntent(CardsIntent.Repeat) } },
                                    modifier = Modifier.weight(1f),
                                )
                                GameButton(
                                    text = stringResource(R.string.know),
                                    enabled = word != null,
                                    onClick = { word?.let { viewModel.onIntent(CardsIntent.Know) } },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
