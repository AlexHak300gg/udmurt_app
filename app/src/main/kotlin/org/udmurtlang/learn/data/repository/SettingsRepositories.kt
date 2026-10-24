package org.udmurtlang.learn.data.repository

import kotlinx.coroutines.flow.Flow
import org.udmurtlang.learn.core.common.TimeProvider
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
) : WordOfDayRepository {

    override suspend fun wordOfDay(): WordOfDay {
        val words = content.allWords().sortedBy { it.id }
        val day = time.today()
        val index = ((day.toEpochDay() % words.size) + words.size).toInt() % words.size
        return WordOfDay(day = day.toString(), word = words[index])
    }
}
