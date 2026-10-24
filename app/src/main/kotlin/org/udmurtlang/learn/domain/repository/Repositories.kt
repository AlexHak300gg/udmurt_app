package org.udmurtlang.learn.domain.repository

import kotlinx.coroutines.flow.Flow
import org.udmurtlang.learn.domain.model.Achievement
import org.udmurtlang.learn.domain.model.DailyActivity
import org.udmurtlang.learn.domain.model.GameResult
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.Streak
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.UserProgress
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.domain.model.WordOfDay

interface ContentRepository {
    suspend fun topics(): List<Topic>
    suspend fun words(topicId: String): List<Word>
    suspend fun word(wordId: String): Word?
    suspend fun allWords(): List<Word>
}

interface ProgressRepository {
    fun observeProgress(): Flow<List<UserProgress>>
    fun observeTopicStatuses(topicId: String): Flow<Map<String, ProgressStatus>>
    suspend fun markAnswer(wordId: String, correct: Boolean)
    suspend fun markKnown(wordId: String)
    suspend fun knownWordIds(): Set<String>
    suspend fun mistakesByCount(): List<String>
}

interface GameRepository {
    suspend fun saveResult(result: GameResult)
    fun observeResults(): Flow<List<GameResult>>
    suspend fun results(): List<GameResult>
    suspend fun totalPlayTimeMs(): Long
}

interface ActivityRepository {
    fun observeDaily(days: Int): Flow<List<DailyActivity>>
    suspend fun daily(days: Int): List<DailyActivity>
    suspend fun streak(): Streak
    suspend fun registerGameplayed(correct: Int, wrong: Int, durationMs: Long)
    suspend fun registerWordsLearned(count: Int)
    suspend fun touchToday()
}

interface WordOfDayRepository {
    suspend fun wordOfDay(): WordOfDay
}

interface AchievementRepository {
    fun observeUnlocked(): Flow<List<Achievement>>
    suspend fun unlockedSlugs(): Set<String>
    suspend fun unlock(slug: String, atMillis: Long)
}
