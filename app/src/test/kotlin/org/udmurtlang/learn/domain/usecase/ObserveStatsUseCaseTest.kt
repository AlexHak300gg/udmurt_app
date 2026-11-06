package org.udmurtlang.learn.domain.usecase

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.UserProgress
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.testing.FakeActivityRepository
import org.udmurtlang.learn.testing.FakeContentRepository
import org.udmurtlang.learn.testing.FakeGameRepository
import org.udmurtlang.learn.testing.FakeProgressRepository

class ObserveStatsUseCaseTest {
    @Test
    fun `one snapshot includes difficult topics and frequently missed words`() = runTest {
        val topics = listOf("complete", "half", "new", "empty").mapIndexed { index, id ->
            Topic(id, id, id, id, 0L, "", index)
        }
        val words = listOf(
            Word("known", "complete", "А", "А", "", ""),
            Word("learned", "half", "Б", "Б", "", ""),
            Word("mistake", "half", "В", "В", "", ""),
            Word("unknown", "new", "Г", "Г", "", ""),
        )
        val progress = FakeProgressRepository().apply {
            this.progress.value = listOf(
                UserProgress("known", ProgressStatus.KNOWN),
                UserProgress("learned", ProgressStatus.KNOWN),
                UserProgress("mistake", wrongCount = 3),
            )
        }
        val snapshot = ObserveStatsUseCase(
            FakeContentRepository(topics, words),
            progress,
            FakeGameRepository(),
            FakeActivityRepository(),
        )().first()

        assertEquals(2, snapshot.wordsKnown)
        assertEquals(listOf("mistake"), snapshot.topMistakes.map { it.id })
        assertEquals(listOf("new", "half"), snapshot.weakTopics.map { it.id })
    }

    @Test
    fun `answering Know updates the open report and weekly learning activity`() = runTest {
        val topic = Topic("numbers", "numbers", "Лыдъёс", "Числа", 0L, "🔢", 0)
        val word = Word("one", topic.id, "Одӥг", "один", "", "1️⃣")
        val progress = FakeProgressRepository()
        val activity = FakeActivityRepository()
        val observeStats = ObserveStatsUseCase(
            FakeContentRepository(listOf(topic), listOf(word)), progress, FakeGameRepository(), activity,
        )

        observeStats().test {
            assertEquals(0, awaitItem().wordsKnown)
            MarkWordKnownUseCase(progress, activity)(word.id)
            var updated = awaitItem()
            while (updated.wordsKnown == 0 || updated.weeklyActivity.sumOf { it.wordsLearned } == 0) {
                updated = awaitItem()
            }
            assertEquals(1, updated.wordsKnown)
            assertEquals(1, updated.weeklyActivity.sumOf { it.wordsLearned })
            assertEquals(0, updated.gamesPlayed)
            assertEquals(emptyList(), updated.weakTopics)
        }
    }
}
