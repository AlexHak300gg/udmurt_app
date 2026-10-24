package org.udmurtlang.learn.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.data.local.db.AchievementDao
import org.udmurtlang.learn.data.local.db.AchievementEntity
import org.udmurtlang.learn.data.local.db.SyncQueueDao
import org.udmurtlang.learn.data.local.db.SyncQueueEntity
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.domain.model.Achievement
import org.udmurtlang.learn.domain.repository.AchievementRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao,
    private val syncQueueDao: SyncQueueDao,
    private val time: TimeProvider,
) : AchievementRepository {

    override fun observeUnlocked(): Flow<List<Achievement>> =
        dao.observeAll().map { list -> list.map { Achievement(it.slug, it.unlockedAt) } }

    override suspend fun unlockedSlugs(): Set<String> = dao.unlockedSlugs().toSet()

    override suspend fun unlock(slug: String, atMillis: Long) {
        dao.insert(AchievementEntity(slug = slug, unlockedAt = atMillis))
        syncQueueDao.insert(
            SyncQueueEntity(
                entityType = ENTITY,
                entityId = slug,
                payload = """{"badge_slug":"$slug","unlocked_at":$atMillis,"client_uuid":"${UUID.randomUUID()}"}""",
                clientUuid = UUID.randomUUID().toString(),
                updatedAt = time.nowMillis(),
            ),
        )
    }

    private companion object {
        const val ENTITY = "achievements"
    }
}
