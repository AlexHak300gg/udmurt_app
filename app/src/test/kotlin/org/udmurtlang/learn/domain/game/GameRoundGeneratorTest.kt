package org.udmurtlang.learn.domain.game

import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameRoundGeneratorTest {

    private val generator = GameRoundGenerator(Random(42))

    private fun word(id: String, topic: String, ru: String = "ru-$id", udm: String = "udm-$id") =
        Word(id = id, topicId = topic, udm = udm, ru = ru, transcription = "[]", emoji = "🌰")

    private fun pool(size: Int = 12) =
        (1..size).map { word("w$it", if (it % 2 == 0) "a" else "b", ru = "слово$it") }

    @Test
    fun `choice round contains correct option and unique options`() {
        val words = pool()
        repeat(20) {
            val round = generator.choice(words.first(), words, optionCount = 4)
            assertEquals(4, round.options.size)
            assertEquals(round.options.distinct(), round.options)
            assertEquals(words.first().ru, round.options[round.correctIndex])
        }
    }

    @Test
    fun `odd one out has three same topic and one odd`() {
        val grouped = mapOf(
            "a" to (1..5).map { word("a$it", "a") },
            "b" to (1..5).map { word("b$it", "b") },
            "c" to (1..5).map { word("c$it", "c") },
        )
        repeat(20) {
            val round = generator.oddOneOut(grouped)
            assertNotNull(round)
            assertEquals(4, round.words.size)
            val odd = round.words[round.oddIndex]
            val others = round.words.filterIndexed { index, _ -> index != round.oddIndex }
            assertTrue(others.all { it.topicId == odd.topicId || it.topicId == others.first().topicId })
            assertEquals(1, round.words.groupingBy { it.topicId }.eachCount().values.count { it == 1 })
        }
    }

    @Test
    fun `odd one out returns null when not enough groups`() {
        val grouped = mapOf("a" to (1..6).map { word("a$it", "a") })
        assertEquals(null, generator.oddOneOut(grouped))
    }

    @Test
    fun `sorting has items from both categories`() {
        val words = (1..6).map { word("l$it", "animals_wild") } +
            (1..6).map { word("r$it", "vegetables") }
        val theme = SortingThemes.all.first { it.id == "animals_plants" }
        val round = generator.sorting(words, theme)
        assertNotNull(round)
        assertEquals(6, round.items.size)
        assertTrue(round.items.any { it.categoryIndex == 0 })
        assertTrue(round.items.any { it.categoryIndex == 1 })
        assertEquals(round.items.map { it.wordId }.distinct().size, round.items.size)
    }

    @Test
    fun `memory cards form matching pairs`() {
        val words = (1..6).map { word("m$it", "a", udm = "удм$it") }
        val round = generator.memory(words, pairCount = 4)
        assertNotNull(round)
        assertEquals(4, round.left.size)
        assertEquals(4, round.right.size)
        assertEquals(
            round.left.map { it.wordId }.sorted(),
            round.right.map { it.wordId }.sorted(),
        )
    }

    @Test
    fun `builder keeps letter multiset`() {
        val words = (1..10).map { word("b$it", "a", udm = "тӥӟбур") }
        repeat(10) {
            val round = generator.builder(words.random(Random(7)))
            assertNotNull(round)
            assertEquals(
                round.udm.lowercase().filter { it.isLetter() }.map { it.toString() }.sorted(),
                round.letters.sorted(),
            )
            assertFalse(round.letters.isEmpty())
        }
    }

    @Test
    fun `true false round consistency`() {
        val words = pool()
        repeat(20) {
            val round = generator.trueFalse(words.first(), words)
            if (round.isTrue) {
                assertEquals(words.first().ru, round.ru)
            } else {
                assertFalse(round.ru == words.first().ru)
            }
        }
    }

    @Test
    fun `alphabet contains udmurt letters`() {
        assertTrue(UdmurtAlphabet.isUdmurtLetter("ӧ"))
        assertTrue(UdmurtAlphabet.isUdmurtLetter("ӟ"))
        assertFalse(UdmurtAlphabet.isUdmurtLetter("a"))
    }

    @Test
    fun `topic model keeps colors`() {
        val topic = Topic("id", "id", "Удмурт", "Удмуртский", 0xFF0E7A3C, "🌲", 1)
        assertEquals(0xFF0E7A3C, topic.color)
    }
}
