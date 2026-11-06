package org.udmurtlang.learn.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.udmurtlang.learn.domain.game.GameScoring
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.StatsSnapshot
import org.udmurtlang.learn.domain.model.UserProgress
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.GameRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import javax.inject.Inject

class ObserveStatsUseCase @Inject constructor(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val games: GameRepository,
    private val activity: ActivityRepository,
) {
    operator fun invoke(): Flow<StatsSnapshot> = flow {
        val allWords = content.allWords()
        val topics = content.topics()
        val wordsById = allWords.associateBy { it.id }
        val wordsByTopic = allWords.groupBy { it.topicId }
        emitAll(combine(
            progress.observeProgress(),
            games.observeResults(),
            activity.observeDaily(7),
            activity.observeStreak(),
        ) { entries, results, daily, streak ->
            val known = entries.filter { it.status == ProgressStatus.KNOWN }.mapTo(hashSetOf()) { it.wordId }
            val topMistakes = entries.filter { it.wrongCount > 0 }
                .sortedWith(compareByDescending(UserProgress::wrongCount).thenByDescending(UserProgress::lastAnswerAt))
                .take(10)
                .mapNotNull { wordsById[it.wordId] }
            val weakTopics = topics
                .mapNotNull { topic ->
                    val words = wordsByTopic[topic.id].orEmpty()
                    val learned = words.count { it.id in known }
                    if (learned == words.size) null else topic to learned.toFloat() / words.size
                }
                .sortedBy { it.second }
                .take(3)
                .map { it.first }
            StatsSnapshot(
                wordsKnown = known.size,
                wordsTotal = allWords.size,
                gamesPlayed = results.size,
                accuracy = GameScoring.accuracy(results.sumOf { it.correct }, results.sumOf { it.total }),
                currentStreak = streak.current,
                bestStreak = streak.best,
                topMistakes = topMistakes,
                weeklyActivity = daily,
                totalPlayTimeMs = results.sumOf { it.durationMs },
                weakTopics = weakTopics,
            )
        })
    }
}
