package org.udmurtlang.learn.data.supabase

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.udmurtlang.learn.domain.repository.SyncQueueRepository
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun syncQueueRepository(): SyncQueueRepository
    fun supabaseProvider(): SupabaseProvider
    fun syncJson(): Json
}

/**
 * Фоновая синхронизация офлайн-очереди с Supabase.
 * Ставится в очередь только при SUPABASE_ENABLED = true.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val entryPoint: SyncWorkerEntryPoint =
        EntryPointAccessors.fromApplication(context, SyncWorkerEntryPoint::class.java)

    override suspend fun doWork(): Result {
        val provider = entryPoint.supabaseProvider()
        if (!provider.isReady) return Result.success()
        val userId = provider.client.auth.currentUserOrNull()?.id ?: return Result.success()
        val syncQueue = entryPoint.syncQueueRepository()
        val json = entryPoint.syncJson()
        return try {
            val tasks = syncQueue.pending(BATCH)
            tasks.groupBy { it.entityType }.forEach { (type, group) ->
                when (type) {
                    "user_progress" -> pushProgress(provider, json, userId, group.map { it.payload })
                    "game_results" -> pushGames(provider, json, userId, group.map { it.payload })
                    "achievements" -> pushAchievements(provider, json, userId, group.map { it.payload })
                    else -> Unit
                }
                syncQueue.markSynced(group.map { it.id })
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun pushProgress(
        provider: SupabaseProvider,
        json: Json,
        userId: String,
        payloads: List<String>,
    ) {
        val rows = payloads.map { payload ->
            val o = json.parseToJsonElement(payload).jsonObject
            RemoteProgress(
                user_id = userId,
                word_id = o.getValue("word_id").jsonPrimitive.content,
                status = o.getValue("status").jsonPrimitive.content,
                correct_count = o.getValue("correct_count").jsonPrimitive.int,
                wrong_count = o.getValue("wrong_count").jsonPrimitive.int,
                last_answer_at = o.getValue("last_answer_at").jsonPrimitive.long,
                client_uuid = o.getValue("client_uuid").jsonPrimitive.content,
                updated_at = o.getValue("updated_at").jsonPrimitive.long,
            )
        }
        provider.client.from("user_progress").upsert(rows) { onConflict = "user_id,word_id" }
    }

    private suspend fun pushGames(
        provider: SupabaseProvider,
        json: Json,
        userId: String,
        payloads: List<String>,
    ) {
        val rows = payloads.map { payload ->
            val o = json.parseToJsonElement(payload).jsonObject
            RemoteGameResult(
                id = o.getValue("id").jsonPrimitive.content,
                user_id = userId,
                game_type = o.getValue("game_type").jsonPrimitive.content,
                score = o.getValue("score").jsonPrimitive.int,
                correct = o.getValue("correct").jsonPrimitive.int,
                total = o.getValue("total").jsonPrimitive.int,
                duration_sec = o.getValue("duration_sec").jsonPrimitive.int,
                played_at = o.getValue("played_at").jsonPrimitive.long,
                client_uuid = o.getValue("client_uuid").jsonPrimitive.content,
            )
        }
        provider.client.from("game_results").upsert(rows) { onConflict = "id" }
    }

    private suspend fun pushAchievements(
        provider: SupabaseProvider,
        json: Json,
        userId: String,
        payloads: List<String>,
    ) {
        val rows = payloads.map { payload ->
            val o = json.parseToJsonElement(payload).jsonObject
            RemoteAchievement(
                user_id = userId,
                badge_slug = o.getValue("badge_slug").jsonPrimitive.content,
                unlocked_at = o.getValue("unlocked_at").jsonPrimitive.long,
            )
        }
        provider.client.from("achievements").upsert(rows) { onConflict = "user_id,badge_slug" }
    }

    companion object {
        private const val WORK_NAME = "supabase_sync"
        private const val BATCH = 200

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
