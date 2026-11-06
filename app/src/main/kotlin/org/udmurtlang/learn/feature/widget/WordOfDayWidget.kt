package org.udmurtlang.learn.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.LocaleList
import android.util.Log
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.Lazy
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.udmurtlang.learn.MainActivity
import org.udmurtlang.learn.R
import org.udmurtlang.learn.domain.model.AppSettings
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.SettingsRepository
import org.udmurtlang.learn.domain.repository.WordOfDayRepository
import java.util.Locale

class WordOfDayWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        requestUpdate(context)
    }

    override fun onDisabled(context: Context) {
        installed.value = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            requestUpdate(context)
        }
    }

    companion object {
        private const val WORK_NAME = "word_of_day_widget"
        private val installed = MutableStateFlow(false)

        internal fun component(context: Context) = ComponentName(context, WordOfDayWidget::class.java)

        private fun requestUpdate(context: Context) {
            installed.value = AppWidgetManager.getInstance(context).getAppWidgetIds(component(context)).isNotEmpty()
            if (!installed.value) return
            try {
                WorkManager.getInstance(context).enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<WordOfDayWidgetWorker>().build(),
                )
            } catch (error: IllegalStateException) {
                Log.w("WordOfDayWidget", "Could not schedule a widget update", error)
            }
        }

        internal suspend fun observeChanges(
            context: Context,
            settings: Flow<AppSettings>,
            activity: Lazy<ActivityRepository>,
        ) {
            installed.value = AppWidgetManager.getInstance(context).getAppWidgetIds(component(context)).isNotEmpty()
            installed.collectLatest { active ->
                if (!active) return@collectLatest
                combine(
                    settings.map { it.uiLanguage }.distinctUntilChanged(),
                    activity.get().observeStreak(),
                ) { language, streak -> language to streak.current }
                    .distinctUntilChanged()
                    .collect { requestUpdate(context) }
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WordOfDayWidgetEntryPoint {
    fun widgetWordOfDay(): WordOfDayRepository
    fun widgetActivity(): ActivityRepository
    fun widgetSettings(): SettingsRepository
}

class WordOfDayWidgetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val manager = AppWidgetManager.getInstance(context)
        val component = WordOfDayWidget.component(context)
        if (manager.getAppWidgetIds(component).isEmpty()) return Result.success()

        try {
            val entry = EntryPointAccessors.fromApplication(context, WordOfDayWidgetEntryPoint::class.java)
            val word = entry.widgetWordOfDay().wordOfDay().word
            val days = entry.widgetActivity().streak().current
            val language = entry.widgetSettings().settings.first().uiLanguage
            val localized = context.createConfigurationContext(Configuration(context.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(language.tag)))
            })
            val openWord = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    action = MainActivity.ACTION_OPEN_WORD_OF_DAY
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val views = RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
                setTextViewText(R.id.widget_title, localized.getString(R.string.word_of_day))
                setTextViewText(R.id.widget_word, "${word.emoji} ${word.udm}")
                setTextViewText(R.id.widget_translation, word.ru)
                setTextViewText(R.id.widget_streak, "\uD83D\uDD25 $days")
                setContentDescription(
                    R.id.widget_streak,
                    "${localized.getString(R.string.current_streak)}: $days ${localized.getString(R.string.streak_days)}",
                )
                setOnClickPendingIntent(R.id.widget_root, openWord)
            }
            currentCoroutineContext().ensureActive()
            manager.updateAppWidget(component, views)
            return Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w("WordOfDayWidget", "Could not update the widget", error)
            return if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }
}
