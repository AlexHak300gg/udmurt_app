package org.udmurtlang.learn.data.supabase

import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.data.local.db.ProgressDao
import org.udmurtlang.learn.data.local.db.ProgressEntity
import org.udmurtlang.learn.data.repository.LocalAccountRepository
import org.udmurtlang.learn.domain.model.Profile
import org.udmurtlang.learn.domain.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ProfilePatch(val display_name: String, val updated_at: Long)

@Serializable
data class AvatarPatch(val avatar: String, val updated_at: Long)

/**
 * Облачная реализация аккаунта: включается только при SUPABASE_ENABLED = true.
 * Профиль отражается в локальной таблице, поэтому UI не меняется.
 */
@Singleton
class SupabaseAccountRepository @Inject constructor(
    private val provider: SupabaseProvider,
    private val local: LocalAccountRepository,
    private val progressDao: ProgressDao,
    private val time: TimeProvider,
) : AccountRepository {

    override val isCloudMode: Boolean get() = provider.isReady

    override fun observeProfile(): Flow<Profile?> = local.observeProfile()

    override suspend fun register(email: String, password: String, name: String): Result<Profile> =
        if (!provider.isReady) {
            local.register(email, password, name)
        } else {
            runCatching {
                provider.client.auth.signUpWith(Email) {
                    this.email = email.trim().lowercase()
                    this.password = password
                }
                val uid = provider.client.auth.currentUserOrNull()?.id
                    ?: return Result.failure(Exception("Письмо с подтверждением отправлено. Проверьте почту."))
                provider.client.from("profiles").upsert(
                    RemoteProfile(id = uid, display_name = name.trim().ifBlank { "Юный лингвист" }),
                ) { onConflict = "id" }
                mergeWithCloud(uid)
                local.attachCloudAccount(uid, email.trim().lowercase(), name)
            }
        }

    override suspend fun login(email: String, password: String): Result<Profile> =
        if (!provider.isReady) {
            local.login(email, password)
        } else {
            runCatching {
                provider.client.auth.signInWith(Email) {
                    this.email = email.trim().lowercase()
                    this.password = password
                }
                val uid = provider.client.auth.currentUserOrNull()?.id
                    ?: error("Не удалось войти. Попробуйте ещё разок!")
                mergeWithCloud(uid)
                local.attachCloudAccount(uid, email.trim().lowercase(), email.substringBefore('@'))
            }
        }

    override suspend fun continueAsGuest(): Profile = local.continueAsGuest()

    override suspend fun logout() {
        if (provider.isReady) runCatching { provider.client.auth.signOut() }
        local.logout()
    }

    override suspend fun updateName(name: String) {
        local.updateName(name)
        val uid = provider.client.auth.currentUserOrNull()?.id ?: return
        provider.client.from("profiles").update(
            ProfilePatch(display_name = name.trim(), updated_at = time.nowMillis()),
        ) { filter { eq("id", uid) } }
    }

    override suspend fun updateAvatar(avatar: String) {
        local.updateAvatar(avatar)
        val uid = provider.client.auth.currentUserOrNull()?.id ?: return
        provider.client.from("profiles").update(
            AvatarPatch(avatar = avatar, updated_at = time.nowMillis()),
        ) { filter { eq("id", uid) } }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> =
        if (!provider.isReady) {
            local.changePassword(oldPassword, newPassword)
        } else {
            runCatching {
                val emailAddr = local.currentEmail()
                provider.client.auth.signInWith(Email) {
                    email = emailAddr
                    password = oldPassword
                }
                provider.client.auth.updateUser { password = newPassword }
            }
        }

    override suspend fun deleteAccount() {
        if (provider.isReady) runCatching { provider.client.auth.signOut() }
        local.deleteAccount()
    }

    /** Слияние: счётчики по слову — максимум, статус known побеждает. */
    private suspend fun mergeWithCloud(userId: String) {
        val localRows = progressDao.all()
        if (localRows.isNotEmpty()) {
            provider.client.from("user_progress").upsert(
                localRows.map {
                    RemoteProgress(
                        user_id = userId,
                        word_id = it.wordId,
                        status = it.status,
                        correct_count = it.correctCount,
                        wrong_count = it.wrongCount,
                        last_answer_at = it.lastAnswerAt,
                        client_uuid = it.clientUuid.ifBlank { it.wordId },
                        updated_at = it.updatedAt,
                    )
                },
            ) { onConflict = "user_id,word_id" }
        }
        val remote = provider.client.from("user_progress")
            .select { filter { eq("user_id", userId) } }
            .decodeList<RemoteProgress>()
        remote.forEach { r ->
            val current = progressDao.get(r.word_id)
            val merged = if (current == null) {
                ProgressEntity(
                    wordId = r.word_id,
                    status = r.status,
                    correctCount = r.correct_count,
                    wrongCount = r.wrong_count,
                    lastAnswerAt = r.last_answer_at,
                    clientUuid = r.client_uuid,
                    updatedAt = r.updated_at,
                )
            } else {
                val maxCorrect = maxOf(current.correctCount, r.correct_count)
                val maxWrong = maxOf(current.wrongCount, r.wrong_count)
                current.copy(
                    status = when {
                        current.status == "known" || r.status == "known" -> "known"
                        maxCorrect > 0 -> "learning"
                        else -> "new"
                    },
                    correctCount = maxCorrect,
                    wrongCount = maxWrong,
                    lastAnswerAt = maxOf(current.lastAnswerAt, r.last_answer_at),
                    updatedAt = maxOf(current.updatedAt, r.updated_at),
                )
            }
            progressDao.upsert(merged)
        }
    }
}
