package org.udmurtlang.learn.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.core.media.FeedbackPort
import org.udmurtlang.learn.domain.model.AppSettings
import org.udmurtlang.learn.domain.model.Achievement
import org.udmurtlang.learn.domain.model.DailyActivity
import org.udmurtlang.learn.domain.model.GameResult
import org.udmurtlang.learn.domain.model.ProgressStatus
import org.udmurtlang.learn.domain.model.Profile
import org.udmurtlang.learn.domain.model.Streak
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.UserProgress
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.domain.model.WordOfDay
import org.udmurtlang.learn.domain.repository.AccountRepository
import org.udmurtlang.learn.domain.repository.AchievementRepository
import org.udmurtlang.learn.domain.repository.ActivityRepository
import org.udmurtlang.learn.domain.repository.ContentRepository
import org.udmurtlang.learn.domain.repository.GameRepository
import org.udmurtlang.learn.domain.repository.ProgressRepository
import org.udmurtlang.learn.domain.repository.SettingsRepository

class FakeTimeProvider(private var now: Long = 1_000L) : TimeProvider {
    override fun nowMillis(): Long = now
    override fun today() = java.time.LocalDate.of(2026, 1, 15)
    fun advance(delta: Long) {
        now += delta
    }
}

class FakeFeedback : FeedbackPort {
    var correct = 0
    var wrong = 0
    var clicks = 0
    var victories = 0
    override fun onCorrect() { correct++ }
    override fun onWrong() { wrong++ }
    override fun onClick() { clicks++ }
    override fun onVictory() { victories++ }
}

class FakeContentRepository(
    private val topics: List<Topic>,
    private val words: List<Word>,
) : ContentRepository {
    override suspend fun topics(): List<Topic> = topics
    override suspend fun words(topicId: String): List<Word> = words.filter { it.topicId == topicId }
    override suspend fun word(wordId: String): Word? = words.find { it.id == wordId }
    override suspend fun allWords(): List<Word> = words
}

class FakeProgressRepository : ProgressRepository {
    val progress = MutableStateFlow<List<UserProgress>>(emptyList())
    val known = mutableSetOf<String>()
    val mistakes = mutableMapOf<String, Int>()

    override fun observeProgress(): Flow<List<UserProgress>> = progress
    override fun observeTopicStatuses(topicId: String): Flow<Map<String, ProgressStatus>> =
        flowOf(known.associateWith { ProgressStatus.KNOWN })

    override suspend fun markAnswer(wordId: String, correct: Boolean) {
        mistakes[wordId] = (mistakes[wordId] ?: 0) + if (correct) 0 else 1
    }

    override suspend fun markKnown(wordId: String) { known += wordId }
    override suspend fun knownWordIds(): Set<String> = known
    override suspend fun mistakesByCount(): List<String> =
        mistakes.entries.sortedByDescending { it.value }.map { it.key }
}

class FakeActivityRepository : ActivityRepository {
    var streak = Streak(current = 2, best = 5)
    var games = 0
    var wordsLearned = 0
    override fun observeDaily(days: Int): Flow<List<DailyActivity>> = flowOf(emptyList())
    override suspend fun daily(days: Int): List<DailyActivity> = emptyList()
    override suspend fun streak(): Streak = streak
    override suspend fun registerGameplayed(correct: Int, wrong: Int, durationMs: Long) { games++ }
    override suspend fun registerWordsLearned(count: Int) { wordsLearned += count }
    override suspend fun touchToday() = Unit
}

class FakeGameRepository : GameRepository {
    val saved = mutableListOf<GameResult>()
    override suspend fun saveResult(result: GameResult) { saved += result }
    override fun observeResults(): Flow<List<GameResult>> = flowOf(saved)
    override suspend fun results(): List<GameResult> = saved
    override suspend fun totalPlayTimeMs(): Long = saved.sumOf { it.durationMs }
}

class FakeAccountRepository : AccountRepository {
    override val isCloudMode: Boolean = false
    private val profileFlow = MutableStateFlow<Profile?>(null)
    var failWith: String? = null
    var registerCalls = 0
    var loginCalls = 0

    override fun observeProfile(): Flow<Profile?> = profileFlow

    override suspend fun register(email: String, password: String, name: String): Result<Profile> {
        registerCalls++
        failWith?.let { return Result.failure(IllegalStateException(it)) }
        val profile = Profile("u1", email, name, "🐿️", isGuest = false)
        profileFlow.value = profile
        return Result.success(profile)
    }

    override suspend fun login(email: String, password: String): Result<Profile> {
        loginCalls++
        failWith?.let { return Result.failure(IllegalStateException(it)) }
        val profile = Profile("u1", email, "Ребёнок", "🐿️", isGuest = false)
        profileFlow.value = profile
        return Result.success(profile)
    }

    override suspend fun continueAsGuest(): Profile {
        val profile = Profile("guest", "", "Гость", "🐿️", isGuest = true)
        profileFlow.value = profile
        return profile
    }

    override suspend fun logout() { profileFlow.value = null }
    override suspend fun updateName(name: String) {
        profileFlow.value = profileFlow.value?.copy(displayName = name)
    }

    override suspend fun updateAvatar(avatar: String) {
        profileFlow.value = profileFlow.value?.copy(avatar = avatar)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun deleteAccount() { profileFlow.value = null }
}

class FakeSettingsRepository(
    initial: AppSettings = AppSettings(),
) : SettingsRepository {
    private val flow = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = flow
    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        flow.value = transform(flow.value)
    }
}

class FakeAchievementRepository : AchievementRepository {
    private val unlocked = MutableStateFlow<List<Achievement>>(emptyList())

    override fun observeUnlocked(): Flow<List<Achievement>> = unlocked
    override suspend fun unlockedSlugs(): Set<String> = unlocked.value.map { it.slug }.toSet()
    override suspend fun unlock(slug: String, atMillis: Long) {
        if (unlocked.value.none { it.slug == slug }) {
            unlocked.value += Achievement(slug, atMillis)
        }
    }
}
