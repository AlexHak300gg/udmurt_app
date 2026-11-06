package org.udmurtlang.learn.feature.wordofday

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import org.udmurtlang.learn.core.media.hasBundledSpeech
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.ZcInk
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcOrangeOnDark
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.components.ConfettiOverlay
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.core.designsystem.studyCardColor
import org.udmurtlang.learn.core.designsystem.vividScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WordOfDayScreen(
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit,
    viewModel: WordOfDayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val wod = state.wod
    val wide = currentWindowClass().isWide
    val short = isShortWindow()

    Box(Modifier.fillMaxSize().background(vividScreen(ZcPurple))) {
        Column(
            modifier = Modifier.fillMaxSize().safeContentTopPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ZcHeader(
                title = stringResource(R.string.word_of_day),
                onBack = onBack,
                tinted = true,
                modifier = Modifier.widthIn(max = ReadingContentWidth),
            )
            ScrollableContent(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                centerVertically = wide || wod == null,
            ) {
                if (wod == null) {
                    EmptyState(
                        title = stringResource(R.string.loading),
                        hint = stringResource(R.string.word_of_day),
                        tinted = true,
                    )
                    return@ScrollableContent
                }
                AdaptivePanes(
                    sideBySide = wide,
                    first = {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Ornament(
                                UdmOrnament.STAR,
                                modifier = Modifier.align(Alignment.TopStart).offset(x = (-35).dp).size(94.dp),
                                color = onVivid().copy(alpha = 0.15f),
                            )
                            Ornament(
                                UdmOrnament.CROSS,
                                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 26.dp).size(74.dp),
                                color = onVivid().copy(alpha = 0.15f),
                            )
                            Surface(shape = CircleShape, color = studyCardColor()) {
                                Box(
                                    modifier = Modifier.size(if (short) 106.dp else 170.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(wod.word.emoji, fontSize = if (short) 58.sp else 88.sp)
                                }
                            }
                        }
                    },
                    second = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(dateLabel(wod.day), style = MaterialTheme.typography.labelSmall, color = ZcOrangeOnDark)
                            Text(
                                wod.word.udm,
                                style = MaterialTheme.typography.displayMedium,
                                textAlign = TextAlign.Center,
                                color = onVivid(),
                            )
                            if (wod.word.transcription.isNotBlank()) {
                                Text("[${wod.word.transcription}]", color = onVividMuted(), style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(wod.word.ru, style = MaterialTheme.typography.titleLarge, color = ZcOrangeOnDark)
                        }
                    },
                )
                val actionModifier = Modifier.fillMaxWidth().padding(top = 14.dp)
                if (state.marked) {
                    Surface(
                        modifier = actionModifier,
                        shape = MaterialTheme.shapes.large,
                        color = brandAccent(ZcTeal),
                        contentColor = ZcInk,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.done), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    GameButton(
                        text = stringResource(R.string.know),
                        onClick = viewModel::markKnown,
                        containerColor = onVivid(),
                        contentColor = ZcInk,
                        modifier = actionModifier,
                    )
                }
                Text(
                    stringResource(R.string.wod_related),
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = onVividMuted(),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    state.related.forEach { word ->
                        Surface(
                            onClick = { viewModel.speakRelated(word) },
                            enabled = hasBundledSpeech(word.udm),
                            color = studyCardColor(),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape,
                        ) {
                            Text(
                                "${word.emoji} ${word.udm}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
            if (wod != null) {
                FlowRow(
                    modifier = Modifier.widthIn(max = ReadingContentWidth).fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    maxItemsInEachRow = if (short) 2 else 1,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GameButton(
                        text = stringResource(R.string.wod_listen),
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        onClick = viewModel::speak,
                        containerColor = brandAccent(ZcOrange),
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    GameButton(
                        text = stringResource(R.string.wod_study_topic),
                        onClick = { onOpenTopic(wod.word.topicId) },
                        containerColor = studyCardColor(),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        ConfettiOverlay(active = state.marked)
    }
}

private fun dateLabel(day: String): String = runCatching {
    LocalDate.parse(day).format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru")))
}.getOrDefault(day)
