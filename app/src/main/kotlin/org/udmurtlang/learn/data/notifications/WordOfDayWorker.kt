package org.udmurtlang.learn.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
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
import java.time.Duration
import java.time.LocalTime
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
        if (!settings.notificationsEnabled) {
            WordOfDayWorker.cancel(applicationContext)
            return Result.success()
        }
        val word = entryPoint.wordOfDayRepository().wordOfDay().word
        entryPoint.wordOfDayNotifier().showWordOfDay(word)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "word_of_day_notify"
        private const val NOTIFY_HOUR = 9

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WordOfDayWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayUntilHour(), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        // До следующих 9:00 локального времени
        private fun delayUntilHour(): Long {
            val now = LocalTime.now()
            val minutes = Duration.between(now, LocalTime.of(NOTIFY_HOUR, 0)).toMinutes()
            return if (minutes <= 0) minutes + Duration.ofDays(1).toMinutes() else minutes
        }
    }
}
