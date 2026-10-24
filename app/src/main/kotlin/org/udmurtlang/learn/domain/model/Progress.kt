package org.udmurtlang.learn.domain.model

enum class ProgressStatus { NEW, LEARNING, KNOWN }

data class UserProgress(
    val wordId: String,
    val status: ProgressStatus = ProgressStatus.NEW,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastAnswerAt: Long = 0L,
)

data class Streak(
    val current: Int = 0,
    val best: Int = 0,
    val lastActiveDay: String = "",
)

data class DailyActivity(
    val day: String,
    val wordsLearned: Int = 0,
    val gamesPlayed: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
)
