package org.udmurtlang.learn.feature.wordofday

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.ConfettiOverlay
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.domain.model.WordOfDay

@Composable
fun WordOfDayScreen(
    onBack: () -> Unit,
    viewModel: WordOfDayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        WordOfDayContent(state = state, onBack = onBack, onSpeak = viewModel::speak, onKnow = viewModel::markKnown)
        StarPattern(
            modifier = Modifier.matchParentSize(),
            color = ZcWhite,
            alpha = 0.07f,
        )
        ConfettiOverlay(active = state.marked)
    }
}

@Composable
private fun WordOfDayContent(
    state: WordOfDayState,
    onBack: () -> Unit,
    onSpeak: () -> Unit,
    onKnow: () -> Unit,
) {
    val wod: WordOfDay? = state.wod
    val wide = currentWindowClass().isWide
    val short = isShortWindow()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleGradient)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = stringResource(R.string.word_of_day),
            onBack = onBack,
            tinted = true,
            modifier = Modifier.widthIn(max = WideContentWidth),
        )
        if (wod == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.word_of_day),
                )
            }
            return@Column
        }

        ScrollableContent(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            maxWidth = WideContentWidth,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            centerVertically = true,
        ) {
            AdaptivePanes(
                sideBySide = wide,
                firstWeight = 0.8f,
                secondWeight = 1.2f,
                first = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = ZcWhite) {
                            Text(
                                text = dateLabel(wod.day),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF4F34AE),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(if (short) 116.dp else 196.dp)
                                .clip(CircleShape)
                                .background(ZcWhite)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = wod.word.emoji, fontSize = if (short) 58.sp else 90.sp)
                        }
                    }
                },
                second = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = wod.word.udm,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displayMedium,
                            color = ZcWhite,
                        )
                        if (wod.word.transcription.isNotBlank()) {
                            Text(
                                text = "(${wod.word.transcription})",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ZcWhite.copy(alpha = 0.72f),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.16f)) {
                            Text(
                                text = stringResource(R.string.word_translation),
                                style = MaterialTheme.typography.labelMedium,
                                color = ZcWhite.copy(alpha = 0.75f),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = wod.word.ru,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                            color = ZcOrange,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.wod_every_day),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = ZcWhite.copy(alpha = 0.8f),
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            maxItemsInEachRow = 2,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            GameButton(
                                text = stringResource(R.string.wod_listen),
                                icon = Icons.Filled.VolumeUp,
                                onClick = onSpeak,
                                gradient = PurpleGradient,
                                modifier = Modifier.weight(1f),
                            )
                            GameButton(
                                text = stringResource(if (state.marked) R.string.done else R.string.know),
                                icon = if (state.marked) Icons.Filled.Check else null,
                                enabled = !state.marked,
                                onClick = onKnow,
                                containerColor = ZcWhite,
                                contentColor = Color(0xFF2A1A16),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                },
            )
        }
    }
}

private val PurpleGradient = Brush.verticalGradient(listOf(Color(0xFF4F34AE), Color(0xFF5B3DB8)))

private fun dateLabel(day: String): String = runCatching {
    LocalDate.parse(day).format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
        .replaceFirstChar { it.uppercase(Locale("ru")) }
}.getOrDefault(day)
