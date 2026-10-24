package org.udmurtlang.learn.domain.repository

import kotlinx.coroutines.flow.Flow
import org.udmurtlang.learn.domain.model.AppSettings
import org.udmurtlang.learn.domain.model.Profile

interface AccountRepository {
    val isCloudMode: Boolean
    fun observeProfile(): Flow<Profile?>
    suspend fun register(email: String, password: String, name: String): Result<Profile>
    suspend fun login(email: String, password: String): Result<Profile>
    suspend fun continueAsGuest(): Profile
    suspend fun logout()
    suspend fun updateName(name: String)
    suspend fun updateAvatar(avatar: String)
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun deleteAccount()
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}

data class SyncTask(
    val id: Long,
    val entityType: String,
    val entityId: String,
    val payload: String,
    val clientUuid: String,
    val updatedAt: Long,
)

/** Очередь офлайн-действий; пустует, пока SUPABASE_ENABLED = false. */
interface SyncQueueRepository {
    suspend fun enqueue(entityType: String, entityId: String, payload: String)
    suspend fun pending(limit: Int): List<SyncTask>
    suspend fun markSynced(ids: List<Long>)
    suspend fun pendingCount(): Int
}
