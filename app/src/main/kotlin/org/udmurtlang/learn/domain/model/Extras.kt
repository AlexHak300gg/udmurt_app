package org.udmurtlang.learn.domain.model

data class WordOfDay(
    val day: String,
    val word: Word,
)

data class Achievement(
    val slug: String,
    val unlockedAt: Long? = null,
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}

object AchievementSlugs {
    const val FIRST_WORD = "first_word"
    const val STREAK_3 = "streak_3"
    const val STREAK_7 = "streak_7"
    const val TOPICS_DONE = "topics_done"
    const val GAMES_10 = "games_10"

    val all = listOf(FIRST_WORD, STREAK_3, STREAK_7, TOPICS_DONE, GAMES_10)
}

data class TopicProgress(
    val topic: Topic,
    val learned: Int,
    val total: Int,
) {
    val isComplete: Boolean get() = total > 0 && learned >= total
}

data class StatsSnapshot(
    val wordsKnown: Int,
    val wordsTotal: Int,
    val gamesPlayed: Int,
    val accuracy: Float,
    val currentStreak: Int,
    val bestStreak: Int,
    val topMistakes: List<Word>,
    val weeklyActivity: List<DailyActivity>,
    val totalPlayTimeMs: Long,
)

data class ChildReport(
    val childName: String,
    val wordsKnown: Int,
    val wordsTotal: Int,
    val gamesPlayed: Int,
    val accuracy: Float,
    val weakTopics: List<Topic>,
    val weekly: List<DailyActivity>,
    val currentStreak: Int,
)
