package org.udmurtlang.learn.domain.game

import org.udmurtlang.learn.domain.model.GameRound
import org.udmurtlang.learn.domain.model.MemorySide
import org.udmurtlang.learn.domain.model.SortItem
import org.udmurtlang.learn.domain.model.Topic
import org.udmurtlang.learn.domain.model.Word
import kotlin.random.Random

object UdmurtAlphabet {
    val letters: List<String> = listOf(
        "а", "б", "в", "г", "д", "ж", "з", "и", "й", "к",
        "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф",
        "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю",
        "я", "ӧ", "ӱ", "ү", "ө", "ӵ", "ӟ", "ӥ", "ӆ", "ӹ",
    )

    fun isUdmurtLetter(letter: String): Boolean = letter in letters
}

data class SortingTheme(
    val id: String,
    val titleRu: String,
    val leftCategory: String,
    val rightCategory: String,
    val leftTopicIds: Set<String>,
    val rightTopicIds: Set<String>,
)

object SortingThemes {
    val all: List<SortingTheme> = listOf(
        SortingTheme(
            id = "animals_plants",
            titleRu = "Животные и растения",
            leftCategory = "Животные",
            rightCategory = "Растения",
            leftTopicIds = setOf("animals_wild", "animals_home"),
            rightTopicIds = setOf("vegetables", "berries"),
        ),
        SortingTheme(
            id = "edible",
            titleRu = "Съедобное и несъедобное",
            leftCategory = "Съедобное",
            rightCategory = "Несъедобное",
            leftTopicIds = setOf("food", "vegetables", "berries"),
            rightTopicIds = setOf("clothes", "animals_wild", "family"),
        ),
    )
}

class GameRoundGenerator(private val random: Random) {

    fun choice(word: Word, pool: List<Word>, optionCount: Int = 4): GameRound.Choice {
        val correct = word.ru
        val others = (pool - word)
            .shuffled(random)
            .map { it.ru }
            .filter { it != correct }
            .distinct()
            .take(optionCount - 1)
        val options = (others + correct).shuffled(random)
        return GameRound.Choice(
            wordId = word.id,
            promptUdm = word.udm,
            promptEmoji = word.emoji,
            options = options,
            correctIndex = options.indexOf(correct),
        )
    }

    fun oddOneOut(wordsByTopic: Map<String, List<Word>>, minGroup: Int = 3): GameRound.OddOneOut? {
        val groups = wordsByTopic.filterValues { it.size >= minGroup + 1 }.keys.toList()
        if (groups.size < 2) return null
        val mainTopic = groups.random(random)
        val otherTopic = (groups - mainTopic).random(random)
        val mainWords = wordsByTopic.getValue(mainTopic).shuffled(random).take(minGroup)
        val odd = wordsByTopic.getValue(otherTopic).random(random)
        val all = (mainWords + odd).shuffled(random)
        return GameRound.OddOneOut(
            wordId = odd.id,
            oddWordId = odd.id,
            words = all,
            oddIndex = all.indexOf(odd),
        )
    }

    fun sorting(
        allWords: List<Word>,
        theme: SortingTheme,
        itemCount: Int = 6,
    ): GameRound.Sorting? {
        val left = allWords.filter { it.topicId in theme.leftTopicIds }.shuffled(random)
        val right = allWords.filter { it.topicId in theme.rightTopicIds }.shuffled(random)
        if (left.isEmpty() || right.isEmpty()) return null
        val takeLeft = (itemCount / 2).coerceIn(1, left.size)
        val takeRight = (itemCount - takeLeft).coerceIn(1, right.size)
        val items = (left.take(takeLeft).map { SortItem(it.id, it.ru, it.emoji, 0) } +
            right.take(takeRight).map { SortItem(it.id, it.ru, it.emoji, 1) }).shuffled(random)
        return GameRound.Sorting(
            wordId = items.first().wordId,
            items = items,
            categoryNames = listOf(theme.leftCategory, theme.rightCategory),
        )
    }

    fun memory(words: List<Word>, pairCount: Int = 4): GameRound.MemoryCard? {
        if (words.size < pairCount) return null
        val chosen = words.shuffled(random).take(pairCount)
        val left = chosen.map { MemorySide(it.id, it.udm, isEmoji = false) }.shuffled(random)
        val right = chosen.map { MemorySide(it.id, it.emoji, isEmoji = true) }.shuffled(random)
        return GameRound.MemoryCard(wordId = chosen.first().id, left = left, right = right)
    }

    fun builder(word: Word): GameRound.Builder? {
        val letters = word.udm.lowercase().filter { it.isLetter() }
            .map { it.toString() }
        if (letters.size < 2) return null
        val scrambled = scramble(letters)
        return GameRound.Builder(
            wordId = word.id,
            udm = word.udm,
            ru = word.ru,
            emoji = word.emoji,
            letters = scrambled,
        )
    }

    fun trueFalse(word: Word, pool: List<Word>): GameRound.TrueFalse {
        val shouldBeWrong = random.nextBoolean() && pool.any { it.id != word.id && it.ru != word.ru }
        if (!shouldBeWrong) {
            return GameRound.TrueFalse(word.id, word.udm, word.ru, word.emoji, isTrue = true)
        }
        val other = (pool - word).filter { it.ru != word.ru }.random(random)
        return GameRound.TrueFalse(word.id, word.udm, other.ru, word.emoji, isTrue = false)
    }

    fun wordOfDayIndex(total: Int, dayEpochDay: Long): Int =
        ((dayEpochDay % total) + total).toInt() % total

    private fun scramble(letters: List<String>): List<String> {
        if (letters.size < 2) return letters
        var result = letters.shuffled(random)
        var guard = 0
        while (result == letters && guard < 8) {
            result = result.shuffled(random)
            guard++
        }
        return result
    }
}
