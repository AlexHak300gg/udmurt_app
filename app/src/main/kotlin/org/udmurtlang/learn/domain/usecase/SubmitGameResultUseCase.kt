package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.game.GameScoring
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.domain.model.GameResult
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.GameRepository
import java.util.UUID
import javax.inject.Inject

class SubmitGameResultUseCase @Inject constructor(
    private val games: GameRepository,
    private val activity: ActivityRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(
        gameType: GameType,
        correct: Int,
        total: Int,
        bestStreak: Int,
        durationMs: Long,
    ): GameResult {
        val result = GameResult(
            id = UUID.randomUUID().toString(),
            gameType = gameType,
            score = GameScoring.score(correct, bestStreak),
            correct = correct,
            total = total,
            durationMs = durationMs,
            playedAt = time.nowMillis(),
        )
        games.saveResult(result)
        activity.registerGameplayed(correct, total - correct, durationMs)
        activity.touchToday()
        return result
    }
}
