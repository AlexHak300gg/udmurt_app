package org.udmurtlang.learn.domain.notifications

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.domain.model.AppSettings
import java.time.LocalTime

/** Порт планировщика локальных уведомлений (слово дня). */
interface NotificationScheduler {
    suspend fun sync(notificationsEnabled: Boolean, time: LocalTime)
}

internal suspend fun NotificationScheduler.observeWordOfDaySchedule(
    settings: Flow<AppSettings>,
    onFailure: (Throwable) -> Unit,
) {
    settings
        .map { it.notificationsEnabled to it.wordOfDayTime }
        .distinctUntilChanged()
        .collect { (enabled, time) ->
            repeat(2) { attempt ->
                try {
                    sync(enabled, time)
                    return@collect
                } catch (error: Exception) {
                    currentCoroutineContext().ensureActive()
                    onFailure(error)
                    if (attempt == 0) delay(500)
                } catch (error: LinkageError) {
                    onFailure(error)
                    return@collect
                }
            }
        }
}
