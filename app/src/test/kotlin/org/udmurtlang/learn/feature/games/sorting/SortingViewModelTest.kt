package org.udmurtlang.learn.feature.games.sorting

import app.cash.turbine.test
import kotlinx.coroutines.test.advanceUntilIdle
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SortingViewModelTest {

    private fun words(): List<Word> {
        val byTopic = mapOf(
            "animals_wild" to "Зверь",
            "animals_home" to "Домашнее",
            "vegetables" to "Овощ",
            "berries" to "Ягода",
        )
        return byTopic.flatMap { (topic, ru) ->
            (1..4).map { n ->
                Word(
                    id = "${topic}_$n",
                    topicId = topic,
                    udm = "$topic-udm$n",
                    ru = "$ru$n",
                    transcription = "[]",
                    emoji = "🥕",
                )
            }
        }
    }

    private val topics = listOf(
        Topic("animals_wild", "animals_wild", "Айгулет", "Дикие животные", 0xFF0E7A3C, "🦌", 1),
        Topic("animals_home", "animals_home", "Вот гизь", "Домашние животные", 0xFF0E7A3C, "🐄", 2),
        Topic("vegetables", "vegetables", "Овощъёс", "Овощи", 0xFF0E7A3C, "🥕", 3),
        Topic("berries", "berries", "Ягодъёс", "Ягоды", 0xFF0E7A3C, "🫐", 4),
    )

    @Test
    fun `placing all items correctly finishes the game`() = runVmTest {
        val content = FakeContentRepository(topics, words())
        val games = FakeGameRepository()
        val feedback = FakeFeedback()
        val progress = FakeProgressRepository()
        val vm = SortingViewModel(
            content = content,
            submit = SubmitGameResultUseCase(games, FakeActivityRepository(), FakeTimeProvider()),
            markAnswer = MarkAnswerUseCase(progress, FakeActivityRepository()),
            feedback = feedback,
            time = FakeTimeProvider(),
        )
        vm.effects.test {
            advanceUntilIdle()
            val round = vm.state.value.round
            assertNotNull(round)
            assertEquals(6, round.items.size)
            round.items.forEachIndexed { index, item ->
                vm.onIntent(SortingIntent.ItemSelected(index))
                vm.onIntent(SortingIntent.CategorySelected(item.categoryIndex))
                advanceUntilIdle()
            }
            assertTrue(vm.state.value.finished)
            val effect = awaitItem() as GameEffect.Finished
            assertEquals(GameType.SORTING.slug, effect.slug)
            assertEquals(6, effect.correct)
            assertEquals(6, effect.total)
            assertEquals(6, feedback.correct)
            assertEquals(1, games.saved.size)
        }
    }

    @Test
    fun `wrong category does not place the item`() = runVmTest {
        val vm = SortingViewModel(
            content = FakeContentRepository(topics, words()),
            submit = SubmitGameResultUseCase(
                FakeGameRepository(),
                FakeActivityRepository(),
                FakeTimeProvider(),
            ),
            markAnswer = MarkAnswerUseCase(FakeProgressRepository(), FakeActivityRepository()),
            feedback = FakeFeedback(),
            time = FakeTimeProvider(),
        )
        advanceUntilIdle()
        val round = vm.state.value.round
        assertNotNull(round)
        val item = round.items.first()
        val wrongCategory = 1 - item.categoryIndex
        vm.onIntent(SortingIntent.ItemSelected(0))
        vm.onIntent(SortingIntent.CategorySelected(wrongCategory))
        advanceUntilIdle()
        assertEquals(0, vm.state.value.placedWordIds.size)
        assertTrue(!vm.state.value.finished)
    }
}
