package org.udmurtlang.learn.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val titleUdm: String,
    val titleRu: String,
    val color: String,
    val emoji: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "words",
    indices = [Index("topicId")],
)
data class WordEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val udm: String,
    val ru: String,
    val transcription: String,
    val emoji: String,
    val exampleUdm: String? = null,
    val exampleRu: String? = null,
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "user_progress",
    indices = [Index("wordId", unique = true), Index("userId"), Index("clientUuid", unique = true)],
)
data class ProgressEntity(
    @PrimaryKey val wordId: String,
    val userId: String = LOCAL_USER,
    val status: String = "new",
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastAnswerAt: Long = 0L,
    val clientUuid: String = "",
    val updatedAt: Long = 0L,
) {
    companion object {
        const val LOCAL_USER = "local"
    }
}

@Entity(tableName = "game_results", indices = [Index("userId"), Index("clientUuid", unique = true)])
data class GameResultEntity(
    @PrimaryKey val id: String,
    val userId: String = ProgressEntity.LOCAL_USER,
    val gameType: String,
    val score: Int,
    val correct: Int,
    val total: Int,
    val durationMs: Long,
    val playedAt: Long,
    val clientUuid: String = "",
)

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val day: String,
    val wordsLearned: Int = 0,
    val gamesPlayed: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
)

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val id: Int = 1,
    val current: Int = 0,
    val best: Int = 0,
    val lastActiveDay: String = "",
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val avatar: String,
    val isGuest: Boolean,
    val passwordHash: String? = null,
    val createdAt: Long,
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val slug: String,
    val unlockedAt: Long,
)

@Entity(
    tableName = "sync_queue",
    indices = [Index("clientUuid", unique = true), Index("synced")],
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val payload: String,
    val clientUuid: String,
    val updatedAt: Long,
    val synced: Boolean = false,
)
