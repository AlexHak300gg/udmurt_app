package org.udmurtlang.learn.feature.games

import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.core.media.FeedbackPort
import org.udmurtlang.learn.domain.game.GameScoring
import org.udmurtlang.learn.domain.model.GameResult
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.SubmitGameResultUseCase

/** Общая игровая сессия: счёт, серии, очки, итог. */
class GameSession(
    private val gameType: GameType,
    private val submit: SubmitGameResultUseCase,
    private val markAnswer: MarkAnswerUseCase,
    private val feedback: FeedbackPort,
    private val time: TimeProvider,
) {
    var correct: Int = 0
        private set
    var streak: Int = 0
        private set
    var bestStreak: Int = 0
        private set

    private val startedAt = time.nowMillis()

    suspend fun answer(wordId: String, isCorrect: Boolean): Boolean {
        markAnswer(wordId, isCorrect)
        if (isCorrect) {
            correct++
            streak++
            bestStreak = maxOf(bestStreak, streak)
            feedback.onCorrect()
        } else {
            streak = 0
            feedback.onWrong()
        }
        return isCorrect
    }

    fun click() = feedback.onClick()

    fun victory() = feedback.onVictory()

    suspend fun finish(total: Int): GameResult =
        submit(
            gameType = gameType,
            correct = correct,
            total = total,
            bestStreak = bestStreak,
            durationMs = time.nowMillis() - startedAt,
        )
}

sealed interface GameEffect {
    data class Finished(
        val slug: String,
        val score: Int,
        val correct: Int,
        val total: Int,
    ) : GameEffect
}

enum class FeedbackKind { CORRECT, WRONG }

fun starsFor(correct: Int, total: Int): Int = GameScoring.stars(correct, total)
