package org.udmurtlang.learn.domain.notifications

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.udmurtlang.learn.domain.model.AppSettings
import java.time.LocalTime

class NotificationSchedulerTest {
    @Test
    fun aFailedReminderDoesNotStopListeningForTheNextTime() = runTest {
        val oldTime = LocalTime.of(9, 0)
        val newTime = LocalTime.of(17, 45)
        val attempts = mutableListOf<LocalTime>()
        val errors = mutableListOf<Throwable>()
        val scheduler = object : NotificationScheduler {
            override suspend fun sync(notificationsEnabled: Boolean, time: LocalTime) {
                attempts += time
                if (time == oldTime) throw IllegalStateException("Cannot schedule the old reminder")
            }
        }
        val enabled = AppSettings(notificationsEnabled = true, wordOfDayTime = oldTime)
        scheduler.observeWordOfDaySchedule(
            flowOf(enabled, enabled.copy(ttsVolume = 0.5f), enabled.copy(wordOfDayTime = newTime)),
            onFailure = { errors += it },
        )
        assertEquals(listOf(oldTime, oldTime, newTime), attempts)
        assertEquals(2, errors.size)
    }
}
