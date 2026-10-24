package org.udmurtlang.learn.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.udmurtlang.learn.domain.model.AchievementSlugs
import org.udmurtlang.learn.domain.model.GameResult
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.model.Streak
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.testing.FakeAchievementRepository
import org.udmurtlang.learn.testing.FakeActivityRepository
import org.udmurtlang.learn.testing.FakeContentRepository
import org.udmurtlang.learn.testing.FakeGameRepository
import org.udmurtlang.learn.testing.FakeProgressRepository
import org.udmurtlang.learn.testing.FakeTimeProvider

class UnlockAchievementsUseCaseTest {

    private val achievements = FakeAchievementRepository()
    private val progress = FakeProgressRepository()
    private val activity = FakeActivityRepository()
    private val games = FakeGameRepository()
    private val time = FakeTimeProvider()
    private val content = FakeContentRepository(
        topics = listOf(Topic("t1", "greeting", "Кылӟеч", "Приветствие", 0xFF0E7A3C, "👋", 0)),
        words = listOf(
            Word("w1", "t1", "Ӟечбур", "здравствуй", "[ӟечбур]", "👋"),
            Word("w2", "t1", "Тау", "спасибо", "[тау]", "🙏"),
        ),
    )

    private suspend fun unlock(): List<String> = UnlockAchievementsUseCase(
        achievements = achievements,
        progress = progress,
        content = content,
        activity = activity,
        games = games,
        time = time,
    ).invoke()

    @Test
    fun `no progress unlocks nothing`() = runTest {
        assertTrue(unlock().isEmpty())
        assertTrue(achievements.unlockedSlugs().isEmpty())
    }

    @Test
    fun `first known word unlocks first_word`() = runTest {
        progress.markKnown("w1")
        assertEquals(listOf(AchievementSlugs.FIRST_WORD), unlock())
        assertEquals(setOf(AchievementSlugs.FIRST_WORD), achievements.unlockedSlugs())
    }

    @Test
    fun `streak of seven unlocks both streak badges`() = runTest {
        activity.streak = Streak(current = 7, best = 7)
        val unlocked = unlock()
        assertEquals(
            setOf(AchievementSlugs.STREAK_3, AchievementSlugs.STREAK_7),
            unlocked.toSet(),
        )
    }

    @Test
    fun `all words known unlocks topics_done`() = runTest {
        progress.markKnown("w1")
        progress.markKnown("w2")
        assertTrue(AchievementSlugs.TOPICS_DONE in unlock())
    }

    @Test
    fun `ten played games unlock games_10`() = runTest {
        repeat(10) {
            games.saved += GameResult("g$it", GameType.CHOICE, 10, 5, 5, 1000, it.toLong())
        }
        assertTrue(AchievementSlugs.GAMES_10 in unlock())
    }

    @Test
    fun `repeat call does not duplicate unlocks`() = runTest {
        progress.markKnown("w1")
        assertEquals(1, unlock().size)
        assertTrue(unlock().isEmpty())
        assertEquals(setOf(AchievementSlugs.FIRST_WORD), achievements.unlockedSlugs())
    }
}
