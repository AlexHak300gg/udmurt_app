package org.udmurtlang.learn.feature.stats

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.ProgressRing
import org.udmurtlang.learn.core.designsystem.components.StatTile
import org.udmurtlang.learn.core.designsystem.components.WeekBarChart
import org.udmurtlang.learn.core.designsystem.components.WordRow
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.domain.model.Topic

private val dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EE", java.util.Locale.forLanguageTag("ru"))

private fun dayLabel(day: String): String = runCatching {
    java.time.LocalDate.parse(day).format(dayFormatter)
}.getOrDefault("")

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snapshot = state.snapshot
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentTopPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = stringResource(R.string.stats_title),
            caption = stringResource(R.string.stats_subtitle),
            onBack = onBack,
            modifier = Modifier.widthIn(max = ReadingContentWidth),
        )
        if (snapshot == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.stats_title),
                )
            }
            return@Column
        }
        Column(
            modifier = Modifier
                .widthIn(max = ReadingContentWidth)
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Красная карточка прогресса
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(brandAccent(ZcRed)),
            ) {
                Ornament(
                    kind = UdmOrnament.HEX,
                    color = ZcWhite.copy(alpha = 0.16f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 24.dp)
                        .size(130.dp),
                )
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProgressRing(
                        progress = snapshot.wordsKnown.toFloat() / snapshot.wordsTotal.coerceAtLeast(1),
                        label = "${(snapshot.wordsKnown.toFloat() / snapshot.wordsTotal.coerceAtLeast(1) * 100).toInt()}%",
                        size = 84.dp,
                        color = ZcWhite,
                        track = ZcWhite.copy(alpha = 0.25f),
                        labelColor = ZcWhite,
                        strokeWidth = 9.dp,
                    )
                    Column(modifier = Modifier.padding(start = 18.dp)) {
                        if (state.displayName.isNotBlank()) {
                            Text(
                                text = state.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = ZcWhite,
                            )
                        }
                        Text(
                            text = stringResource(R.string.stats_learned_fmt, snapshot.wordsKnown),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = ZcWhite,
                        )
                        Text(
                            text = stringResource(R.string.stats_learned_of, snapshot.wordsTotal),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZcWhite.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            // Три плитки
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = "${snapshot.gamesPlayed}",
                    caption = stringResource(R.string.stats_cap_games),
                    icon = Icons.Outlined.SportsEsports,
                    iconTint = brandAccent(ZcPurple),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "${(snapshot.accuracy * 100).toInt()}%",
                    caption = stringResource(R.string.stats_cap_accuracy),
                    icon = Icons.Outlined.CheckCircle,
                    iconTint = brandAccent(ZcTeal),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "${snapshot.currentStreak}",
                    caption = stringResource(R.string.stats_cap_days),
                    icon = Icons.Filled.LocalFireDepartment,
                    iconTint = brandAccent(ZcOrange),
                    modifier = Modifier.weight(1f),
                )
            }

            // Активность — белая карточка, как в макете
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.activity_week),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(R.string.activity_7),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    WeekBarChart(
                        values = snapshot.weeklyActivity.map { it.correct },
                        labels = snapshot.weeklyActivity.map { dayLabel(it.day) },
                    )
                }
            }

            if (snapshot.weakTopics.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.report_weak_topics),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                snapshot.weakTopics.forEach { ReviewTopicRow(it) }
            }

            // Слабые слова
            Text(
                text = stringResource(R.string.mistake_words),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (snapshot.topMistakes.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_mistakes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                snapshot.topMistakes.take(4).forEach { word ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        WordRow(word = word)
                    }
                }
            }

        }
        val displayName = state.displayName.ifBlank { stringResource(R.string.guest_label) }
        val shareText = buildString {
            appendLine("${stringResource(R.string.report_title)} — $displayName")
            appendLine(stringResource(R.string.you_learned_words, snapshot.wordsKnown))
            appendLine("${stringResource(R.string.accuracy)}: ${(snapshot.accuracy * 100).toInt()}%")
            appendLine("${stringResource(R.string.games_played)}: ${snapshot.gamesPlayed}")
            appendLine("${stringResource(R.string.streak_days)}: ${snapshot.currentStreak}")
            if (snapshot.weakTopics.isNotEmpty()) {
                appendLine()
                appendLine(stringResource(R.string.report_weak_topics))
                snapshot.weakTopics.forEach { appendLine("• ${it.titleRu} / ${it.titleUdm}") }
            }
        }
        GameButton(
            text = stringResource(R.string.report_share),
            icon = Icons.Filled.Share,
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, null))
            },
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier
                .widthIn(max = ReadingContentWidth)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun ReviewTopicRow(topic: Topic) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(topic.emoji, fontSize = 20.sp)
                }
            }
            Column {
                Text(topic.titleUdm, style = MaterialTheme.typography.titleSmall)
                Text(
                    topic.titleRu,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
