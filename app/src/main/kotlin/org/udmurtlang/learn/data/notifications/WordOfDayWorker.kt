package org.udmurtlang.learn.data.notifications

import android.content.Context
import androidx.concurrent.futures.await
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import org.udmurtlang.learn.core.notifications.WordOfDayNotifier
import org.udmurtlang.learn.domain.repository.SettingsRepository
import org.udmurtlang.learn.domain.repository.WordOfDayRepository
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WordOfDayWorkerEntryPoint {
    fun settingsRepository(): SettingsRepository
    fun wordOfDayRepository(): WordOfDayRepository
    fun wordOfDayNotifier(): WordOfDayNotifier
}

/**
 * Ежедневное «Слово дня»; молчит, пока уведомления выключены.
 */
class WordOfDayWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val entryPoint: WordOfDayWorkerEntryPoint =
        EntryPointAccessors.fromApplication(context, WordOfDayWorkerEntryPoint::class.java)

    override suspend fun doWork(): Result {
        val settings = entryPoint.settingsRepository().settings.first()
        val time = settings.wordOfDayTime
        if (!settings.notificationsEnabled || timeTag(time) !in tags) return Result.success()

        val now = ZonedDateTime.now()
        // Update this request only. An older worker cannot replace the new schedule.
        WorkManager.getInstance(applicationContext).updateWork(request(time, id, now)).await()
        if (!now.isBefore(now.toLocalDate().atTime(time).atZone(now.zone))) {
            val word = entryPoint.wordOfDayRepository().wordOfDay().word
            entryPoint.wordOfDayNotifier().showWordOfDay(word)
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "word_of_day_notify"

        fun timeTag(time: LocalTime) = "word_of_day_at_$time"

        fun request(time: LocalTime, id: UUID = UUID.randomUUID(), now: ZonedDateTime = ZonedDateTime.now()) =
            PeriodicWorkRequestBuilder<WordOfDayWorker>(1, TimeUnit.DAYS)
                .setId(id)
                .addTag(timeTag(time))
                .setNextScheduleTimeOverride(nextWordOfDayTime(now, time).toInstant().toEpochMilli())
                .build()
    }
}

internal fun nextWordOfDayTime(now: ZonedDateTime, time: LocalTime): ZonedDateTime {
    val today = now.toLocalDate()
    val todayAtTime = today.atTime(time).atZone(now.zone)
    return if (now.isBefore(todayAtTime)) todayAtTime else today.plusDays(1).atTime(time).atZone(now.zone)
}
