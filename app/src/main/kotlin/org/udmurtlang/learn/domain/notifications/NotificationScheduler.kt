package org.udmurtlang.learn.domain.notifications

/** Порт планировщика локальных уведомлений (слово дня). */
interface NotificationScheduler {
    fun sync(notificationsEnabled: Boolean)
}
