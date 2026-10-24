package org.udmurtlang.learn.feature.learning

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.udmurtlang.learn.core.designsystem.ZcPurple
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        ZcHeader(
            title = stringResource(R.string.topics_title),
            caption = if (topics.isEmpty()) null else stringResource(
                R.string.topics_count,
                topics.size,
                topics.sumOf { it.total },
            ),
            onBack = onBack,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            continueItem?.let { active ->
                item(key = "continue_${active.topic.id}") {
                    ContinueCard(item = active, onClick = { onOpenTopic(active.topic.id) })
                }
            }
            items(topics, key = { it.topic.id }) { item ->
                val isContinue = item == continueItem
                if (!isContinue) {
                    TopicRow(item = item, onClick = { onOpenTopic(item.topic.id) })
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/** Фиолетовая карточка «Продолжить» с прогресс-баром. */
@Composable
private fun ContinueCard(item: TopicProgress, onClick: () -> Unit) {
    val progress = if (item.total == 0) 0f else item.learned.toFloat() / item.total
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(ZcPurple, ZcPurpleDeep)))
                .padding(horizontal = 14.dp, vertical = 14.dp),
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
                        text = item.topic.titleRu,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = item.topic.titleUdm,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
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

/** Обычная строка темы: белая карточка, эмодзи, прогресс справа. */
@Composable
private fun TopicRow(item: TopicProgress, onClick: () -> Unit) {
    val progress = if (item.total == 0) 0f else item.learned.toFloat() / item.total
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.topic.emoji, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.topic.titleRu,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.topic.titleUdm,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.isComplete) {
                Text("✅", fontSize = 18.sp)
            }
            Text(
                text = "${item.learned}/${item.total}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (item.isComplete) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
