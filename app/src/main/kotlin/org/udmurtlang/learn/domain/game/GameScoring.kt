package org.udmurtlang.learn.domain.game

object GameScoring {
    const val POINTS_PER_CORRECT = 10
    const val STREAK_BONUS = 2

    fun score(correct: Int, bestStreak: Int): Int =
        correct * POINTS_PER_CORRECT + bestStreak * STREAK_BONUS

    fun stars(correct: Int, total: Int): Int {
        if (total <= 0) return 0
        val accuracy = correct.toFloat() / total
        return when {
            accuracy >= 0.9f -> 3
            accuracy >= 0.7f -> 2
            else -> 1
        }
    }

    fun accuracy(correct: Int, total: Int): Float =
        if (total <= 0) 0f else correct.toFloat() / total
}
