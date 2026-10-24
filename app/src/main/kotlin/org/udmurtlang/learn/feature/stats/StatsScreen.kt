package org.udmurtlang.learn.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcRedBottom
import org.udmurtlang.learn.core.designsystem.ZcRedTop
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.ProgressLine
import org.udmurtlang.learn.core.designsystem.components.ProgressRing
import org.udmurtlang.learn.core.designsystem.components.StatTile
import org.udmurtlang.learn.core.designsystem.components.WeekBarChart
import org.udmurtlang.learn.core.designsystem.components.WordRow
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.domain.model.StatsSnapshot

private val dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EE", java.util.Locale("ru"))

private fun dayLabel(day: String): String = runCatching {
    java.time.LocalDate.parse(day).format(dayFormatter)
}.getOrDefault("")

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onOpenReport: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snapshot = state.snapshot
    val brand = LocalBrand.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        ZcHeader(
            title = stringResource(R.string.stats_title),
            caption = stringResource(R.string.stats_subtitle),
            onBack = onBack,
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Красная карточка прогресса
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.horizontalGradient(listOf(ZcRedTop, ZcRedBottom))),
            ) {
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
                    emoji = "🎮",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "${(snapshot.accuracy * 100).toInt()}%",
                    caption = stringResource(R.string.stats_cap_accuracy),
                    emoji = "🎯",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "${snapshot.currentStreak}",
                    caption = stringResource(R.string.stats_cap_days),
                    emoji = "🔥",
                    modifier = Modifier.weight(1f),
                )
            }

            // Активность
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

            // Прогресс-линия слов
            ProgressLine(
                progress = snapshot.wordsKnown.toFloat() / snapshot.wordsTotal.coerceAtLeast(1),
                fill = if (brand.isDark) MaterialTheme.colorScheme.primary else ZcRed,
            )

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

            Spacer(modifier = Modifier.height(2.dp))
            GameButton(
                text = stringResource(R.string.report_title),
                icon = Icons.Filled.Description,
                onClick = onOpenReport,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
            )
        }
    }
}
