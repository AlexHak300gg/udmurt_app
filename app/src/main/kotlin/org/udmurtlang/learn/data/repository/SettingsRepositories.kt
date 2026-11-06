package org.udmurtlang.learn.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.core.media.SpeechAudioAssets
import org.udmurtlang.learn.data.local.AppDataStore
import org.udmurtlang.learn.domain.model.AppSettings
import org.udmurtlang.learn.domain.model.WordOfDay
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.SettingsRepository
import org.udmurtlang.learn.domain.repository.WordOfDayRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: AppDataStore,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.settings

    override suspend fun update(transform: (AppSettings) -> AppSettings) =
        dataStore.updateSettings(transform)
}

@Singleton
class WordOfDayRepositoryImpl @Inject constructor(
    private val content: ContentRepository,
    private val time: TimeProvider,
    @ApplicationContext private val context: Context,
) : WordOfDayRepository {

    private val recordings by lazy { context.assets.list(SpeechAudioAssets.DIRECTORY).orEmpty().toSet() }

    override suspend fun wordOfDay(): WordOfDay {
        val allWords = content.allWords().sortedBy { it.id }
        val wordsWithAudio = withContext(Dispatchers.IO) {
            allWords.filter { SpeechAudioAssets.path(it.udm).substringAfterLast('/') in recordings }
        }
        val words = wordsWithAudio.ifEmpty { allWords }
        val day = time.today()
        val index = ((day.toEpochDay() % words.size) + words.size).toInt() % words.size
        return WordOfDay(day = day.toString(), word = words[index])
    }
}
