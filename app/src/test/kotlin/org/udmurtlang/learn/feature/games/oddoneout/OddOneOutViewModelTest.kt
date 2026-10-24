package org.udmurtlang.learn.feature.games.oddoneout

import app.cash.turbine.test
import kotlinx.coroutines.test.advanceUntilIdle
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.domain.usecase.GetWordsGroupedByTopicUseCase
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.SubmitGameResultUseCase
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.testing.FakeActivityRepository
import org.udmurtlang.learn.testing.FakeContentRepository
import org.udmurtlang.learn.testing.FakeFeedback
import org.udmurtlang.learn.testing.FakeGameRepository
import org.udmurtlang.learn.testing.FakeProgressRepository
import org.udmurtlang.learn.testing.FakeTimeProvider
import org.udmurtlang.learn.testing.runVmTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OddOneOutViewModelTest {

    private fun words(): List<Word> = listOf("a", "b", "c").flatMap { topic ->
        (1..5).map { n ->
            Word(
                id = "$topic$n",
                topicId = topic,
                udm = "$topic-udm$n",
                ru = "$topic-ru$n",
                transcription = "[]",
                emoji = "🌰",
            )
        }
    }

    private fun vm(
        content: FakeContentRepository = FakeContentRepository(
            topics = emptyList(),
            words = words(),
        ),
        progress: FakeProgressRepository = FakeProgressRepository(),
        activity: FakeActivityRepository = FakeActivityRepository(),
        games: FakeGameRepository = FakeGameRepository(),
        feedback: FakeFeedback = FakeFeedback(),
        time: FakeTimeProvider = FakeTimeProvider(),
    ) = OddOneOutViewModel(
        getWordsGrouped = GetWordsGroupedByTopicUseCase(content),
        submit = SubmitGameResultUseCase(games, activity, time),
        markAnswer = MarkAnswerUseCase(progress, activity),
        feedback = feedback,
        time = time,
    ) to Triple(games, feedback, progress)

    @Test
    fun `answering all rounds correctly finishes with full score`() = runVmTest {
        val (vm, deps) = vm()
        val (games, feedback, _) = deps
        vm.effects.test {
            advanceUntilIdle()
            val total = vm.state.value.rounds.size
            assertTrue(total > 0, "rounds should be generated")
            repeat(total) {
                val round = vm.state.value.current ?: error("round missing")
                vm.onIntent(OddOneOutIntent.Select(round.oddIndex))
                advanceUntilIdle()
            }
            val effect = awaitItem()
            assertTrue(effect is GameEffect.Finished)
            effect as GameEffect.Finished
            assertEquals(GameType.ODD_ONE_OUT.slug, effect.slug)
            assertEquals(total, effect.correct)
            assertEquals(total, effect.total)
            assertEquals(total * 12, effect.score)
            assertEquals(total, feedback.correct)
            assertEquals(1, games.saved.size)
        }
    }

    @Test
    fun `wrong answers do not increase correct count`() = runVmTest {
        val (vm, deps) = vm()
        val (games, feedback, progress) = deps
        vm.effects.test {
            advanceUntilIdle()
            val total = vm.state.value.rounds.size
            repeat(total) {
                val round = vm.state.value.current ?: error("round missing")
                val wrongIndex = round.words.indices.first { it != round.oddIndex }
                vm.onIntent(OddOneOutIntent.Select(wrongIndex))
                advanceUntilIdle()
            }
            val effect = awaitItem() as GameEffect.Finished
            assertEquals(0, effect.correct)
            assertEquals(total, effect.total)
            assertEquals(0, feedback.correct)
            assertEquals(total, feedback.wrong)
            assertEquals(total, progress.mistakes.values.sum())
            assertEquals(1, games.saved.size)
        }
    }
}
