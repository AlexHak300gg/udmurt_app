package org.udmurtlang.learn.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.data.local.db.ActivityDao
import org.udmurtlang.learn.data.local.db.DailyActivityEntity
import org.udmurtlang.learn.data.local.db.GameResultDao
import org.udmurtlang.learn.data.local.db.GameResultEntity
import org.udmurtlang.learn.data.local.db.StreakEntity
import org.udmurtlang.learn.data.local.db.SyncQueueDao
import org.udmurtlang.learn.data.local.db.SyncQueueEntity
import org.udmurtlang.learn.data.mapper.toDomain
import org.udmurtlang.learn.domain.model.DailyActivity
import org.udmurtlang.learn.domain.model.GameResult
import org.udmurtlang.learn.domain.model.Streak
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.GameRepository
import org.udmurtlang.learn.domain.repository.SyncQueueRepository
import org.udmurtlang.learn.domain.repository.SyncTask
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val dao: GameResultDao,
    private val syncQueue: SyncQueueRepository,
) : GameRepository {

    override suspend fun saveResult(result: GameResult) {
        val entity = GameResultEntity(
            id = result.id,
            gameType = result.gameType.slug,
            score = result.score,
            correct = result.correct,
            total = result.total,
            durationMs = result.durationMs,
            playedAt = result.playedAt,
            clientUuid = UUID.randomUUID().toString(),
        )
        dao.insert(entity)
        syncQueue.enqueue(
            ENTITY_GAMES,
            result.id,
            """{"id":"${result.id}","game_type":"${result.gameType.slug}","score":${result.score},"correct":${result.correct},"total":${result.total},"duration_sec":${result.durationMs / 1000},"played_at":${result.playedAt},"client_uuid":"${entity.clientUuid}"}""",
        )
    }

    override fun observeResults(): Flow<List<GameResult>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun results(): List<GameResult> = dao.all().map { it.toDomain() }

    override suspend fun totalPlayTimeMs(): Long = dao.totalDuration()

    private companion object {
        const val ENTITY_GAMES = "game_results"
    }
}

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val dao: ActivityDao,
    private val time: TimeProvider,
) : ActivityRepository {

    override fun observeDaily(days: Int): Flow<List<DailyActivity>> =
        dao.observeSince(sinceDay(days)).map { stored ->
            fillDays(stored.map { it.toDomain() }, days)
        }

    override suspend fun daily(days: Int): List<DailyActivity> =
        fillDays(dao.since(sinceDay(days)).map { it.toDomain() }, days)

    override suspend fun streak(): Streak =
        dao.streak()?.let { Streak(it.current, it.best, it.lastActiveDay) } ?: Streak()

    override suspend fun registerGameplayed(correct: Int, wrong: Int, durationMs: Long) {
        val today = time.today().toString()
        val current = dao.byDay(today) ?: DailyActivityEntity(day = today)
        dao.upsert(
            current.copy(
                gamesPlayed = current.gamesPlayed + 1,
                correct = current.correct + correct,
                wrong = current.wrong + wrong,
            ),
        )
    }

    override suspend fun registerWordsLearned(count: Int) {
        val today = time.today().toString()
        val current = dao.byDay(today) ?: DailyActivityEntity(day = today)
        dao.upsert(current.copy(wordsLearned = current.wordsLearned + count))
    }

    override suspend fun touchToday() {
        val today = time.today()
        val todayKey = today.toString()
        val current = dao.streak() ?: StreakEntity()
        if (current.lastActiveDay == todayKey) return
        val currentStreak = if (current.lastActiveDay == today.minusDays(1).toString()) {
            current.current + 1
        } else {
            1
        }
        dao.upsertStreak(
            current.copy(
                current = currentStreak,
                best = maxOf(current.best, currentStreak),
                lastActiveDay = todayKey,
            ),
        )
    }

    private fun sinceDay(days: Int): String = time.today().minusDays((days - 1).toLong()).toString()

    private fun fillDays(stored: List<DailyActivity>, days: Int): List<DailyActivity> {
        val byDay = stored.associateBy { it.day }
        val today = time.today()
        return (days - 1 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong()).toString()
            byDay[day] ?: DailyActivity(day = day)
        }
    }
}

@Singleton
class SyncQueueRepositoryImpl @Inject constructor(
    private val dao: SyncQueueDao,
    private val time: TimeProvider,
) : SyncQueueRepository {

    override suspend fun enqueue(entityType: String, entityId: String, payload: String) {
        dao.insert(
            SyncQueueEntity(
                entityType = entityType,
                entityId = entityId,
                payload = payload,
                clientUuid = UUID.randomUUID().toString(),
                updatedAt = time.nowMillis(),
            ),
        )
        if (dao.pendingCount() > QUEUE_LIMIT) dao.trim(QUEUE_LIMIT)
    }

    override suspend fun pending(limit: Int): List<SyncTask> =
        dao.pending(limit).map {
            SyncTask(it.id, it.entityType, it.entityId, it.payload, it.clientUuid, it.updatedAt)
        }

    override suspend fun markSynced(ids: List<Long>) = dao.markSynced(ids)

    override suspend fun pendingCount(): Int = dao.pendingCount()

    private companion object {
        const val QUEUE_LIMIT = 1000
    }
}
