package org.udmurtlang.learn.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.data.local.db.ProgressDao
import org.udmurtlang.learn.data.local.db.ProgressEntity
import org.udmurtlang.learn.data.mapper.toDomain
import org.udmurtlang.learn.data.mapper.toProgressStatus
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.UserProgress
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import org.udmurtlang.learn.domain.repository.SyncQueueRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val dao: ProgressDao,
    private val content: ContentRepository,
    private val syncQueue: SyncQueueRepository,
    private val time: TimeProvider,
) : ProgressRepository {

    override fun observeProgress(): Flow<List<UserProgress>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeTopicStatuses(topicId: String): Flow<Map<String, ProgressStatus>> =
        dao.observeAll().map { list ->
            list.associate { it.wordId to it.status.toProgressStatus() }
        }

    override suspend fun markAnswer(wordId: String, correct: Boolean) {
        val now = time.nowMillis()
        val current = dao.get(wordId) ?: ProgressEntity(
            wordId = wordId,
            clientUuid = UUID.randomUUID().toString(),
        )
        val correctCount = current.correctCount + if (correct) 1 else 0
        val status = when {
            correctCount >= KNOWN_THRESHOLD -> ProgressStatus.KNOWN.name.lowercase()
            correct -> ProgressStatus.LEARNING.name.lowercase()
            current.status == ProgressStatus.KNOWN.name.lowercase() ->
                ProgressStatus.LEARNING.name.lowercase()
            else -> current.status
        }
        val updated = current.copy(
            status = status,
            correctCount = correctCount,
            wrongCount = current.wrongCount + if (correct) 0 else 1,
            lastAnswerAt = now,
            updatedAt = now,
        )
        dao.upsert(updated)
        syncQueue.enqueue(ENTITY_PROGRESS, wordId, updated.toSyncPayload())
    }

    override suspend fun markKnown(wordId: String) {
        val now = time.nowMillis()
        val current = dao.get(wordId) ?: ProgressEntity(
            wordId = wordId,
            clientUuid = UUID.randomUUID().toString(),
        )
        val updated = current.copy(
            status = ProgressStatus.KNOWN.name.lowercase(),
            correctCount = maxOf(current.correctCount, KNOWN_THRESHOLD),
            lastAnswerAt = now,
            updatedAt = now,
        )
        dao.upsert(updated)
        syncQueue.enqueue(ENTITY_PROGRESS, wordId, updated.toSyncPayload())
    }

    override suspend fun knownWordIds(): Set<String> = dao.knownWordIds().toSet()

    override suspend fun mistakesByCount(): List<String> = dao.mistakesByCount()

    private fun ProgressEntity.toSyncPayload(): String =
        """{"word_id":"$wordId","status":"$status","correct_count":$correctCount,"wrong_count":$wrongCount,"last_answer_at":$lastAnswerAt,"client_uuid":"$clientUuid","updated_at":$updatedAt}"""

    private companion object {
        const val KNOWN_THRESHOLD = 2
        const val ENTITY_PROGRESS = "user_progress"
    }
}
