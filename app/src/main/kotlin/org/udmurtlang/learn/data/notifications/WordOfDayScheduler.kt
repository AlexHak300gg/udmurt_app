package org.udmurtlang.learn.data.notifications

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.udmurtlang.learn.domain.notifications.NotificationScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordOfDayScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationScheduler {

    override fun sync(notificationsEnabled: Boolean) {
        if (notificationsEnabled) {
            WordOfDayWorker.schedule(context)
        } else {
            WordOfDayWorker.cancel(context)
        }
    }
}
