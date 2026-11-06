package org.udmurtlang.learn.data.notifications

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.await
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import org.udmurtlang.learn.domain.notifications.NotificationScheduler
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordOfDayScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationScheduler {

    override suspend fun sync(notificationsEnabled: Boolean, time: LocalTime) {
        val workManager = WorkManager.getInstance(context)
        if (!notificationsEnabled) {
            workManager.cancelUniqueWork(WordOfDayWorker.WORK_NAME).await()
            return
        }
        val alreadyScheduled = try {
            workManager.getWorkInfosForUniqueWork(WordOfDayWorker.WORK_NAME)
                .await()
                .any { !it.state.isFinished && WordOfDayWorker.timeTag(time) in it.tags }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // A failed lookup must not prevent replacing the unique reminder.
            Log.w("WordOfDayScheduler", "Could not read the current reminder", error)
            false
        }
        if (alreadyScheduled) return

        workManager.enqueueUniquePeriodicWork(
            WordOfDayWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            WordOfDayWorker.request(time),
        ).await()
    }
}
