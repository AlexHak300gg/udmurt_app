package org.udmurtlang.learn.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.domain.model.Achievement
import org.udmurtlang.learn.domain.model.AchievementSlugs
import org.udmurtlang.learn.domain.repository.AchievementRepository
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.GameRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import javax.inject.Inject

class ObserveAchievementsUseCase @Inject constructor(
    private val achievements: AchievementRepository,
) {
    operator fun invoke(): Flow<List<Achievement>> = achievements.observeUnlocked()
}

/**
 * Пересчитывает условия всех медалей; возвращает только что открытые.
 * Идемпотентно — повторный вызов ничего не дублирует.
 */
class UnlockAchievementsUseCase @Inject constructor(
    private val achievements: AchievementRepository,
    private val progress: ProgressRepository,
    private val content: ContentRepository,
    private val activity: ActivityRepository,
    private val games: GameRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(): List<String> {
        val already = achievements.unlockedSlugs()
        val known = progress.knownWordIds()
        val streak = activity.streak()
        val totalWords = content.allWords().size
        val played = games.results().size

        val met = buildList {
            if (known.isNotEmpty()) add(AchievementSlugs.FIRST_WORD)
            if (streak.current >= STREAK_3_DAYS) add(AchievementSlugs.STREAK_3)
            if (streak.current >= STREAK_7_DAYS) add(AchievementSlugs.STREAK_7)
            if (totalWords > 0 && known.size >= totalWords) add(AchievementSlugs.TOPICS_DONE)
            if (played >= GAMES_10_COUNT) add(AchievementSlugs.GAMES_10)
        }

        val now = time.nowMillis()
        return met.filterNot { it in already }
            .onEach { achievements.unlock(it, now) }
    }

    private companion object {
        const val STREAK_3_DAYS = 3
        const val STREAK_7_DAYS = 7
        const val GAMES_10_COUNT = 10
    }
}
