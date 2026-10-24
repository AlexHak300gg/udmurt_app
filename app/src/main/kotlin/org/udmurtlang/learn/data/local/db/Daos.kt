package org.udmurtlang.learn.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM topics ORDER BY sortOrder")
    suspend fun topics(): List<TopicEntity>

    @Query("SELECT * FROM words WHERE topicId = :topicId ORDER BY sortOrder")
    suspend fun words(topicId: String): List<WordEntity>

    @Query("SELECT * FROM words")
    suspend fun allWords(): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun word(id: String): WordEntity?

    @Query("SELECT COUNT(*) FROM words WHERE topicId = :topicId")
    suspend fun wordCount(topicId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun topicCount(): Int
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress")
    fun observeAll(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE wordId = :wordId")
    suspend fun get(wordId: String): ProgressEntity?

    @Query("SELECT wordId FROM user_progress WHERE status = 'known'")
    suspend fun knownWordIds(): List<String>

    @Query(
        """
        SELECT wordId FROM user_progress
        WHERE wrongCount > 0
        ORDER BY wrongCount DESC, lastAnswerAt DESC
        """,
    )
    suspend fun mistakesByCount(): List<String>

    @Query("SELECT * FROM user_progress")
    suspend fun all(): List<ProgressEntity>

    @Query("SELECT * FROM user_progress WHERE wordId IN (:wordIds)")
    suspend fun byWords(wordIds: List<String>): List<ProgressEntity>

    @Upsert
    suspend fun upsert(entity: ProgressEntity)
}

@Dao
interface GameResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GameResultEntity)

    @Query("SELECT * FROM game_results ORDER BY playedAt DESC")
    fun observeAll(): Flow<List<GameResultEntity>>

    @Query("SELECT * FROM game_results ORDER BY playedAt DESC")
    suspend fun all(): List<GameResultEntity>

    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM game_results")
    suspend fun totalDuration(): Long
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM daily_activity WHERE day >= :sinceDay ORDER BY day")
    fun observeSince(sinceDay: String): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity WHERE day >= :sinceDay ORDER BY day")
    suspend fun since(sinceDay: String): List<DailyActivityEntity>

    @Query("SELECT * FROM daily_activity WHERE day = :day")
    suspend fun byDay(day: String): DailyActivityEntity?

    @Upsert
    suspend fun upsert(entity: DailyActivityEntity)

    @Query("SELECT * FROM streaks WHERE id = 1")
    suspend fun streak(): StreakEntity?

    @Upsert
    suspend fun upsertStreak(entity: StreakEntity)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isGuest = 0 ORDER BY createdAt LIMIT 1")
    fun observeActive(): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun observeById(id: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun byId(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE isGuest = 0 ORDER BY createdAt LIMIT 1")
    suspend fun active(): AccountEntity?

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun byEmail(email: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT slug FROM achievements")
    suspend fun unlockedSlugs(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: AchievementEntity)
}

@Dao
interface SyncQueueDao {
    @Insert
    suspend fun insert(entity: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY updatedAt LIMIT :limit")
    suspend fun pending(limit: Int): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE synced = 0")
    suspend fun pendingCount(): Int

    @Query("UPDATE sync_queue SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM sync_queue WHERE id NOT IN (SELECT id FROM sync_queue ORDER BY updatedAt DESC LIMIT :keep)")
    suspend fun trim(keep: Int)
}
