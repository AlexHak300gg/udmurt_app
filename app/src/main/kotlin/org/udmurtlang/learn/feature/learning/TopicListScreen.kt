package org.udmurtlang.learn.feature.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.columns
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.gridSpacing
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcPurpleDeep
import org.udmurtlang.learn.core.designsystem.components.EmptyState
import org.udmurtlang.learn.core.designsystem.components.ProgressLine
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.domain.model.TopicProgress
import org.udmurtlang.learn.domain.usecase.GetTopicsUseCase
import javax.inject.Inject

@HiltViewModel
class TopicListViewModel @Inject constructor(
    private val getTopics: GetTopicsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(emptyList<TopicProgress>())
    val state: StateFlow<List<TopicProgress>> = _state

    init {
        viewModelScope.launch { _state.value = getTopics() }
    }
}

@Composable
fun TopicListScreen(
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit,
    viewModel: TopicListViewModel = hiltViewModel(),
) {
    val topics by viewModel.state.collectAsStateWithLifecycle()
    val window = currentWindowClass()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentTopPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = stringResource(R.string.topics_title),
            caption = if (topics.isEmpty()) null else stringResource(
                R.string.topics_count,
                topics.size,
                topics.sumOf { it.total },
            ),
            onBack = onBack,
            modifier = Modifier.widthIn(max = WideContentWidth),
        )
        if (topics.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.loading),
                    hint = stringResource(R.string.cards_hint),
                )
            }
            return@Column
        }
        val continueItem = topics.firstOrNull { !it.isComplete && it.learned > 0 }
            ?: topics.firstOrNull { it.learned == 0 && it.total > 0 }
        LazyVerticalGrid(
            columns = GridCells.Fixed(window.columns()),
            modifier = Modifier
                .widthIn(max = WideContentWidth)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(window.gridSpacing),
            horizontalArrangement = Arrangement.spacedBy(window.gridSpacing),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            continueItem?.let { active ->
                item(key = "continue_${active.topic.id}", span = { GridItemSpan(maxLineSpan) }) {
                    ContinueCard(item = active, onClick = { onOpenTopic(active.topic.id) })
                }
            }
            itemsIndexed(topics.filterNot { it == continueItem }, key = { _, item -> item.topic.id }) { index, item ->
                TopicRow(
                    item = item,
                    accent = brandAccent(topicAccents[index % topicAccents.size]),
                    onClick = { onOpenTopic(item.topic.id) },
                )
            }
        }
    }
}

/** Фиолетовая карточка «Продолжить» с прогресс-баром. */
@Composable
private fun ContinueCard(item: TopicProgress, onClick: () -> Unit) {
    val progress = if (item.total == 0) 0f else item.learned.toFloat() / item.total
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.background(brandAccent(ZcPurple))) {
        Ornament(
            kind = UdmOrnament.PINWHEEL,
            color = Color.White.copy(alpha = 0.14f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp)
                .size(120.dp),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(item.topic.emoji, fontSize = 22.sp)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = stringResource(R.string.topic_continue),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                    Text(
                        text = item.topic.titleUdm,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    if (!item.topic.titleRu.equals(item.topic.titleUdm, ignoreCase = true)) {
                        Text(
                            text = item.topic.titleRu,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${item.learned}/${item.total}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            ProgressLine(
                progress = progress,
                fill = Color.White,
                track = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        }
    }
}

/** Акценты строк тем по кругу, как в макете. */
private val topicAccents = listOf(ZcRed, ZcPurple, ZcOrange, ZcTeal)

/** Строка темы по макету: эмодзи в цветной плашке, название, прогресс, счётчик. */
@Composable
private fun TopicRow(item: TopicProgress, accent: Color, onClick: () -> Unit) {
    val progress = if (item.total == 0) 0f else item.learned.toFloat() / item.total
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.topic.emoji, fontSize = 26.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.topic.titleUdm,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!item.topic.titleRu.equals(item.topic.titleUdm, ignoreCase = true)) {
                    Text(
                        text = item.topic.titleRu,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ProgressLine(
                    progress = progress,
                    fill = accent,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (item.isComplete) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                Text(
                    text = "${item.learned}/${item.total}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}
