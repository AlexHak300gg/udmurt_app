package org.udmurtlang.learn.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.udmurtlang.learn.domain.model.Word
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordOfDayNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(org.udmurtlang.learn.R.string.notifications_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(org.udmurtlang.learn.R.string.word_of_day)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun showWordOfDay(word: Word) {
        createChannel()
        if (!canNotify()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(org.udmurtlang.learn.R.drawable.ic_notification)
            .setContentTitle(context.getString(org.udmurtlang.learn.R.string.word_of_day))
            .setContentText("${word.udm} — ${word.ru}")
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Гонщик разрешений: пользователь только что отклонил POST_NOTIFICATIONS
        }
    }

    fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "word_of_day"
        private const val NOTIFICATION_ID = 42
    }
}
