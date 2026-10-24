package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.game.GameScoring
import org.udmurtlang.learn.domain.model.ChildReport
import org.udmurtlang.learn.domain.model.StatsSnapshot
import org.udmurtlang.learn.domain.model.Topic
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
        return StatsSnapshot(
            wordsKnown = known.size,
            wordsTotal = allWords.size,
            gamesPlayed = results.size,
            accuracy = GameScoring.accuracy(correct, total),
            currentStreak = streak.current,
            bestStreak = streak.best,
            topMistakes = mistakeIds.mapNotNull { id -> allWords.find { it.id == id } },
            weeklyActivity = activity.daily(7),
            totalPlayTimeMs = games.totalPlayTimeMs(),
        )
    }
}

class BuildChildReportUseCase @Inject constructor(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val games: GameRepository,
    private val activity: ActivityRepository,
) {
    suspend operator fun invoke(childName: String): ChildReport {
        val allWords = content.allWords()
        val known = progress.knownWordIds()
        val topics = content.topics()
        val weakTopics: List<Topic> = topics
            .map { topic ->
                val words = content.words(topic.id)
                val ratio = if (words.isEmpty()) 1f else words.count { it.id in known }.toFloat() / words.size
                topic to ratio
            }
            .sortedBy { it.second }
            .filter { it.second < 1f }
            .take(3)
            .map { it.first }
        val results = games.results()
        val correct = results.sumOf { it.correct }
        val total = results.sumOf { it.total }
        val streak = activity.streak()
        return ChildReport(
            childName = childName,
            wordsKnown = known.size,
            wordsTotal = allWords.size,
            gamesPlayed = results.size,
            accuracy = GameScoring.accuracy(correct, total),
            weakTopics = weakTopics,
            weekly = activity.daily(7),
            currentStreak = streak.current,
        )
    }
}
