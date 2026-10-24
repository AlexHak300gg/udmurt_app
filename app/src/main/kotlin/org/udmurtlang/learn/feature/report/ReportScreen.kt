package org.udmurtlang.learn.feature.report

import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.ZcInk
import org.udmurtlang.learn.core.designsystem.ZcInkPill
import org.udmurtlang.learn.core.designsystem.ZcRedTop
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.GhostButton
import org.udmurtlang.learn.core.designsystem.components.StatTile
import org.udmurtlang.learn.core.designsystem.components.WeekBarChart
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted

@Composable
fun ReportScreen(
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val report = state.report

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        ZcHeader(
            title = stringResource(R.string.report_title),
            caption = stringResource(R.string.report_intro),
            onBack = onBack,
        )
        val currentReport = report
        if (currentReport == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                GhostButton(text = stringResource(R.string.loading), onClick = {})
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
            // Красная hero-карта статистики
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(ZcRedTop, Color(0xFFF0433B))))
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                ) {
                    Text(
                        text = currentReport.childName.ifBlank { stringResource(R.string.guest_label) },
                        style = MaterialTheme.typography.titleMedium,
                        color = onVivid(),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.words_learned),
                        style = MaterialTheme.typography.labelMedium,
                        color = onVividMuted(),
                    )
                    Text(
                        text = stringResource(R.string.stats_learned_fmt, currentReport.wordsKnown),
                        style = MaterialTheme.typography.displayMedium,
                        color = onVivid(),
                    )
                    Text(
                        text = stringResource(R.string.stats_learned_of, currentReport.wordsTotal),
                        style = MaterialTheme.typography.bodySmall,
                        color = onVividMuted(),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            value = "${currentReport.gamesPlayed}",
                            caption = stringResource(R.string.stats_cap_games),
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = "${(currentReport.accuracy * 100).toInt()}%",
                            caption = stringResource(R.string.stats_cap_accuracy),
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = "${currentReport.currentStreak}",
                            caption = stringResource(R.string.stats_cap_days),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.activity_week),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            WeekBarChart(
                values = currentReport.weekly.map { it.correct },
                labels = currentReport.weekly.map { dayLabelShort(it.day) },
            )

            if (currentReport.weakTopics.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.report_weak_topics),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                currentReport.weakTopics.forEach { topic ->
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
                                color = Color(0xFFFBE7DA),
                                contentColor = ZcInk,
                                modifier = Modifier.size(42.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(topic.emoji, fontSize = 20.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = topic.titleUdm,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = topic.titleRu,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            val shareText = buildString {
                appendLine("${stringResource(R.string.report_title)} — ${currentReport.childName}")
                appendLine(stringResource(R.string.you_learned_words, currentReport.wordsKnown))
                appendLine("${stringResource(R.string.accuracy)}: ${(currentReport.accuracy * 100).toInt()}%")
                appendLine("${stringResource(R.string.games_played)}: ${currentReport.gamesPlayed}")
                appendLine("${stringResource(R.string.streak_days)}: ${currentReport.currentStreak}")
            }
            GameButton(
                text = stringResource(R.string.report_share),
                icon = Icons.Filled.Share,
                containerColor = ZcInkPill,
                contentColor = ZcWhite,
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

internal fun dayLabelShort(day: String): String = runCatching {
    java.time.LocalDate.parse(day).format(
        java.time.format.DateTimeFormatter.ofPattern("EE", java.util.Locale("ru")),
    )
}.getOrDefault("")
