package org.udmurtlang.learn

import android.app.Application
import android.util.Log
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.udmurtlang.learn.data.supabase.SyncWorker
import org.udmurtlang.learn.domain.notifications.NotificationScheduler
import org.udmurtlang.learn.domain.notifications.observeWordOfDaySchedule
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.SettingsRepository
import org.udmurtlang.learn.feature.widget.WordOfDayWidget
import javax.inject.Inject

@HiltAndroidApp
class LearnApp : Application() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var activityRepository: Lazy<ActivityRepository>

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
            notificationScheduler.observeWordOfDaySchedule(settingsRepository.settings) { error ->
                Log.w("LearnApp", "Could not apply the Word of Day reminder settings", error)
            }
        }
        appScope.launch {
            WordOfDayWidget.observeChanges(this@LearnApp, settingsRepository.settings, activityRepository)
        }
    }
}
