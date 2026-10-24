package org.udmurtlang.learn.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.data.account.PasswordHasher
import org.udmurtlang.learn.data.local.AppDataStore
import org.udmurtlang.learn.data.local.db.AccountDao
import org.udmurtlang.learn.data.local.db.AccountEntity
import org.udmurtlang.learn.domain.model.Profile
import org.udmurtlang.learn.domain.repository.AccountRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAccountRepository @Inject constructor(
    private val dao: AccountDao,
    private val dataStore: AppDataStore,
    private val hasher: PasswordHasher,
    private val time: TimeProvider,
) : AccountRepository {

    override val isCloudMode: Boolean = false

    override fun observeProfile(): Flow<Profile?> =
        dataStore.sessionId.flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(null) else dao.observeById(sessionId).mapOrNull()
        }

    override suspend fun register(email: String, password: String, name: String): Result<Profile> {
        val normalized = email.trim().lowercase()
        if (dao.byEmail(normalized) != null) {
            return Result.failure(Exception("Аккаунт с таким e-mail уже есть"))
        }
        val entity = AccountEntity(
            id = UUID.randomUUID().toString(),
            email = normalized,
            displayName = name.trim().ifBlank { "Юный лингвист" },
            avatar = DEFAULT_AVATAR,
            isGuest = false,
            passwordHash = hasher.hash(password),
            createdAt = time.nowMillis(),
        )
        dao.upsert(entity)
        dataStore.setSessionId(entity.id)
        return Result.success(entity.toProfile())
    }

    override suspend fun login(email: String, password: String): Result<Profile> {
        val normalized = email.trim().lowercase()
        val entity = dao.byEmail(normalized)
            ?: return Result.failure(Exception("Нет такого e-mail. Зарегистрируйтесь!"))
        val hash = entity.passwordHash
            ?: return Result.failure(Exception("Это гостевой аккаунт. Создайте пароль."))
        if (!hasher.matches(password, hash)) {
            return Result.failure(Exception("Пароль не подошёл. Ещё разок!"))
        }
        dataStore.setSessionId(entity.id)
        return Result.success(entity.toProfile())
    }

    override suspend fun continueAsGuest(): Profile {
        val sessionId = dataStore.sessionId.first()
        sessionId?.let { dao.byId(it) }?.let { return it.toProfile() }
        val guest = AccountEntity(
            id = UUID.randomUUID().toString(),
            email = GUEST_EMAIL,
            displayName = "Гость",
            avatar = DEFAULT_AVATAR,
            isGuest = true,
            passwordHash = null,
            createdAt = time.nowMillis(),
        )
        dao.upsert(guest)
        dataStore.setSessionId(guest.id)
        return guest.toProfile()
    }

    override suspend fun logout() {
        val sessionId = dataStore.sessionId.first() ?: return
        val entity = dao.byId(sessionId)
        if (entity?.isGuest == true) dao.delete(entity.id)
        dataStore.setSessionId(null)
    }

    override suspend fun updateName(name: String) {
        currentOrNull()?.let { dao.upsert(it.copy(displayName = name.trim())) }
    }

    override suspend fun updateAvatar(avatar: String) {
        currentOrNull()?.let { dao.upsert(it.copy(avatar = avatar)) }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        val entity = currentOrNull()
            ?: return Result.failure(Exception("Нет активного аккаунта"))
        val hash = entity.passwordHash
            ?: return Result.failure(Exception("У гостевого аккаунта нет пароля"))
        if (!hasher.matches(oldPassword, hash)) {
            return Result.failure(Exception("Старый пароль не подошёл"))
        }
        dao.upsert(entity.copy(passwordHash = hasher.hash(newPassword)))
        return Result.success(Unit)
    }

    override suspend fun deleteAccount() {
        val sessionId = dataStore.sessionId.first() ?: return
        dao.delete(sessionId)
        dataStore.setSessionId(null)
    }

    /** Отражение облачного аккаунта в локальной таблице (единый UI для обоих режимов). */
    suspend fun attachCloudAccount(userId: String, email: String, name: String): Profile {
        val entity = AccountEntity(
            id = userId,
            email = email,
            displayName = name.trim().ifBlank { "Юный лингвист" },
            avatar = DEFAULT_AVATAR,
            isGuest = false,
            passwordHash = null,
            createdAt = time.nowMillis(),
        )
        dao.upsert(entity)
        dataStore.setSessionId(entity.id)
        return entity.toProfile()
    }

    suspend fun currentEmail(): String =
        currentOrNull()?.email ?: ""

    private suspend fun currentOrNull(): AccountEntity? =
        dataStore.sessionId.first()?.let { dao.byId(it) }

    private fun AccountEntity.toProfile() = Profile(
        id = id,
        email = email,
        displayName = displayName,
        avatar = avatar,
        isGuest = isGuest,
    )

    private fun Flow<AccountEntity?>.mapOrNull(): Flow<Profile?> = map { it?.toProfile() }

    private companion object {
        const val GUEST_EMAIL = "guest@local"
        const val DEFAULT_AVATAR = "🐿️"
    }
}
