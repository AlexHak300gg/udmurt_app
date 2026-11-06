package org.udmurtlang.learn.domain.notifications

import java.time.LocalTime

/** Порт планировщика локальных уведомлений (слово дня). */
interface NotificationScheduler {
    suspend fun sync(notificationsEnabled: Boolean, time: LocalTime)
}
