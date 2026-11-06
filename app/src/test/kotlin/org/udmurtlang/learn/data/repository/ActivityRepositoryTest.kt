package org.udmurtlang.learn.data.repository

import java.time.LocalDate
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.data.local.db.ActivityDao
import org.udmurtlang.learn.data.local.db.DailyActivityEntity
import org.udmurtlang.learn.data.local.db.StreakEntity

class ActivityRepositoryTest {
    private val today = LocalDate.of(2026, 11, 6)
    private val time = object : TimeProvider {
        override fun today() = today
        override fun nowMillis() = 0L
    }

    @Test
    fun `first activity starts the streak at one`() = runTest {
        val repository = ActivityRepositoryImpl(StreakDao(null), time)
        assertEquals(0, repository.streak().current)
        repository.touchToday()
        repository.touchToday()
        assertEquals(1, repository.streak().current)
    }

    @Test
    fun `an undated saved count cannot create a current streak`() = runTest {
        val repository = ActivityRepositoryImpl(StreakDao(StreakEntity(current = 1, best = 1)), time)
        assertEquals(0, repository.streak().current)
    }

    @Test
    fun `expired streak resets current days and keeps the best`() = runTest {
        val saved = StreakEntity(current = 3, best = 6, lastActiveDay = today.minusDays(2).toString())
        val repository = ActivityRepositoryImpl(StreakDao(saved), time)
        assertEquals(0, repository.streak().current)
        assertEquals(6, repository.streak().best)
        repository.touchToday()
        assertEquals(1, repository.streak().current)
    }

    private class StreakDao(private var saved: StreakEntity?) : ActivityDao {
        override suspend fun streak() = saved
        override suspend fun upsertStreak(entity: StreakEntity) {
            saved = entity
        }
        override suspend fun since(sinceDay: String) = emptyList<DailyActivityEntity>()
        override fun observeSince(sinceDay: String) = flowOf(emptyList<DailyActivityEntity>())
        override suspend fun byDay(day: String): DailyActivityEntity? = null
        override suspend fun upsert(entity: DailyActivityEntity) = Unit
    }
}
