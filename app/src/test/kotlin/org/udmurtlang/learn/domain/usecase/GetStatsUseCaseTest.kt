package org.udmurtlang.learn.domain.usecase

import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.testing.FakeActivityRepository
import org.udmurtlang.learn.testing.FakeContentRepository
import org.udmurtlang.learn.testing.FakeGameRepository
import org.udmurtlang.learn.testing.FakeProgressRepository

class GetStatsUseCaseTest {
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
            known += listOf("known", "learned")
            mistakes["mistake"] = 3
        }
        val snapshot = GetStatsUseCase(
            FakeContentRepository(topics, words),
            progress,
            FakeGameRepository(),
            FakeActivityRepository(),
        )()

        assertEquals(2, snapshot.wordsKnown)
        assertEquals(listOf("mistake"), snapshot.topMistakes.map { it.id })
        assertEquals(listOf("new", "half"), snapshot.weakTopics.map { it.id })
    }
}
