package org.udmurtlang.learn.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.udmurtlang.learn.data.local.db.ProgressEntity
import org.udmurtlang.learn.data.mapper.toDomain
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.testing.FakeContentRepository
import org.udmurtlang.learn.testing.FakeProgressRepository

class ObserveTopicsUseCaseTest {
    @Test
    fun statusUpdatesWhenTheLastWordIsLearned() = runTest {
        val topic = Topic("numbers", "numbers", "Лыдъёс", "Числа", 0, "🔢", 0)
        val word = Word("numbers_0", topic.id, "Одӥг", "один", "одыг", "1️⃣")
        val progress = FakeProgressRepository()
        val observeTopics = ObserveTopicsUseCase(FakeContentRepository(listOf(topic), listOf(word)), progress)
        observeTopics().test {
            assertFalse(awaitItem().single().isComplete)
            progress.progress.value = listOf(ProgressEntity(wordId = word.id, status = "known").toDomain())
            val updated = awaitItem().single()
            assertTrue(updated.isComplete)
            assertEquals(1, updated.learned)
        }
    }
}
