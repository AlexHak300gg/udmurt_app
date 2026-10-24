package org.udmurtlang.learn

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.udmurtlang.learn.data.supabase.SyncWorker
import org.udmurtlang.learn.domain.notifications.NotificationScheduler
import org.udmurtlang.learn.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltAndroidApp
class LearnApp : Application() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationScheduler: NotificationScheduler

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Очередь синхронизации активна только в облачном режиме
        if (BuildConfig.SUPABASE_ENABLED) {
            SyncWorker.schedule(this)
        } else {
            SyncWorker.cancel(this)
        }
        // Согласовать уведомления с сохранённой настройкой
        appScope.launch {
            notificationScheduler.sync(settingsRepository.settings.first().notificationsEnabled)
        }
    }
}
