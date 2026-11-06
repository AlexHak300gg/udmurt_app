package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.game.GameScoring
import org.udmurtlang.learn.domain.model.StatsSnapshot
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.GameRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val games: GameRepository,
    private val activity: ActivityRepository,
) {
    suspend operator fun invoke(): StatsSnapshot {
        val allWords = content.allWords()
        val known = progress.knownWordIds()
        val results = games.results()
        val streak = activity.streak()
        val correct = results.sumOf { it.correct }
        val total = results.sumOf { it.total }
        val mistakeIds = progress.mistakesByCount().take(10)
        val wordsById = allWords.associateBy { it.id }
        val wordsByTopic = allWords.groupBy { it.topicId }
        val weakTopics = content.topics()
            .mapNotNull { topic ->
                val words = wordsByTopic[topic.id].orEmpty()
                val learned = words.count { it.id in known }
                if (learned == words.size) null else topic to learned.toFloat() / words.size
            }
            .sortedBy { it.second }
            .take(3)
            .map { it.first }
        return StatsSnapshot(
            wordsKnown = known.size,
            wordsTotal = allWords.size,
            gamesPlayed = results.size,
            accuracy = GameScoring.accuracy(correct, total),
            currentStreak = streak.current,
            bestStreak = streak.best,
            topMistakes = mistakeIds.mapNotNull(wordsById::get),
            weeklyActivity = activity.daily(7),
            totalPlayTimeMs = games.totalPlayTimeMs(),
            weakTopics = weakTopics,
        )
    }
}
