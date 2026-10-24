package org.udmurtlang.learn.data.supabase

import kotlinx.serialization.Serializable

// DTO 1-в-1 со схемой supabase/migrations/0001_init.sql

@Serializable
data class RemoteProfile(
    val id: String,
    val display_name: String,
    val avatar: String = "🐿️",
    val is_child: Boolean = true,
    val locale: String = "ru",
    val created_at: String? = null,
    val updated_at: String? = null,
)

@Serializable
data class RemoteProgress(
    val user_id: String,
    val word_id: String,
    val status: String = "new",
    val correct_count: Int = 0,
    val wrong_count: Int = 0,
    val last_answer_at: Long = 0,
    val client_uuid: String,
    val updated_at: Long = 0,
)

@Serializable
data class RemoteGameResult(
    val id: String,
    val user_id: String,
    val game_type: String,
    val score: Int,
    val correct: Int,
    val total: Int,
    val duration_sec: Int,
    val played_at: Long,
    val client_uuid: String,
)

@Serializable
data class RemoteDailyActivity(
    val user_id: String,
    val day: String,
    val words_learned: Int = 0,
    val games_played: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
)

@Serializable
data class RemoteStreak(
    val user_id: String,
    val current_streak: Int = 0,
    val best_streak: Int = 0,
    val last_active_day: String = "",
)

@Serializable
data class RemoteAchievement(
    val user_id: String,
    val badge_slug: String,
    val unlocked_at: Long = 0,
)
