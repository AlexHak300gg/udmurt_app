package org.udmurtlang.learn.feature.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.media.hasBundledSpeech
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.vividScreen
import org.udmurtlang.learn.core.designsystem.components.ConfettiOverlay
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.GhostButton
import org.udmurtlang.learn.core.designsystem.components.ProgressLine
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.components.WordCard
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.core.media.AutoPronounce
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun WordCardsScreen(
    onBack: () -> Unit,
    viewModel: WordCardsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val word = state.current.takeUnless { state.finished }
    AutoPronounce(word?.id) {
        word?.let { viewModel.onIntent(CardsIntent.Speak(it)) }
    }

    val wide = currentWindowClass().isWide
    val short = isShortWindow()
    val bg = vividScreen(ZcTeal)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        StarPattern(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            color = if (LocalBrand.current.isDark) ZcTeal else ZcWhite,
            alpha = 0.18f,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentTopPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ZcHeader(
                title = state.topic?.titleUdm ?: stringResource(R.string.topics_title),
                onBack = onBack,
                tinted = true,
                compact = true,
                modifier = Modifier.widthIn(max = WideContentWidth),
                trailing = {
                    Text(
                        text = "${(state.index + 1).coerceAtMost(state.words.size)} / ${state.words.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = onVivid(),
                    )
                },
            )
            if (state.words.isNotEmpty()) {
                ProgressLine(
                    progress = (state.index + 1).toFloat() / state.words.size,
                    modifier = Modifier.widthIn(max = WideContentWidth).padding(horizontal = 20.dp),
                    fill = onVivid(),
                    track = onVivid().copy(alpha = 0.25f),
                    height = 4.dp,
                )
            }
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val cardHeight = if (!wide && !short) (maxHeight - 144.dp).coerceAtLeast(270.dp) else 0.dp
                ScrollableContent(
                    modifier = Modifier.fillMaxSize(),
                    maxWidth = WideContentWidth,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    centerVertically = wide || word == null,
                ) {
                    when {
                        state.finished -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth().padding(12.dp),
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
                                    text = stringResource(R.string.done),
                                    onClick = onBack,
                                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier.padding(top = 24.dp).widthIn(max = 320.dp).fillMaxWidth(),
                                )
                                PreviousWordButton(
                                    enabled = !state.answering,
                                    onClick = { viewModel.onIntent(CardsIntent.Previous) },
                                    modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(),
                                )
                            }
                        }
                        word == null -> {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                EmptyState(
                                    title = stringResource(R.string.loading),
                                    hint = stringResource(R.string.cards_hint),
                                    tinted = true,
                                )
                            }
                        }
                        else -> {
                            AdaptivePanes(
                                sideBySide = wide,
                                firstWeight = 1.8f,
                                secondWeight = 1f,
                                first = {
                                    WordCard(
                                        word = word,
                                        onSpeak = if (hasBundledSpeech(word.udm)) {
                                            { viewModel.onIntent(CardsIntent.Speak(word)) }
                                        } else null,
                                        speakLabel = stringResource(R.string.pronunciation),
                                        translationLabel = stringResource(R.string.word_translation),
                                        sideBySide = wide,
                                        minHeight = cardHeight,
                                    )
                                },
                                second = {
                                    val scheme = MaterialTheme.colorScheme
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        if (state.reviewing) {
                                            GameButton(
                                                text = stringResource(R.string.next),
                                                containerColor = scheme.inverseSurface,
                                                contentColor = scheme.inverseOnSurface,
                                                onClick = { viewModel.onIntent(CardsIntent.Next) },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        } else {
                                            BoxWithConstraints(Modifier.fillMaxWidth()) {
                                                val textWidth = maxWidth / LocalDensity.current.fontScale.coerceAtLeast(1f)
                                                FlowRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    maxItemsInEachRow = if (wide || textWidth < 280.dp) 1 else 2,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                ) {
                                                    GhostButton(
                                                        text = stringResource(R.string.not_known),
                                                        containerColor = scheme.surface,
                                                        contentColor = scheme.onSurface,
                                                        borderColor = scheme.onSurfaceVariant,
                                                        enabled = !state.answering,
                                                        onClick = { viewModel.onIntent(CardsIntent.Repeat) },
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                    GameButton(
                                                        text = stringResource(R.string.know),
                                                        containerColor = scheme.inverseSurface,
                                                        contentColor = scheme.inverseOnSurface,
                                                        enabled = !state.answering,
                                                        onClick = { viewModel.onIntent(CardsIntent.Know) },
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                }
                                            }
                                        }
                                        PreviousWordButton(
                                            enabled = state.index > 0 && !state.answering,
                                            onClick = { viewModel.onIntent(CardsIntent.Previous) },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
        ConfettiOverlay(active = state.finished, modifier = Modifier.matchParentSize())
    }
}

@Composable
private fun PreviousWordButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = onVivid()),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.previous_word), textAlign = TextAlign.Center)
    }
}
