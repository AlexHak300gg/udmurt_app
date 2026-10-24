package org.udmurtlang.learn.domain.model

enum class GameType(val slug: String) {
    CHOICE("choice"),
    ODD_ONE_OUT("odd_one_out"),
    SORTING("sorting"),
    MEMORY("memory"),
    WORD_BUILDER("word_builder"),
    TRUE_FALSE("true_false"),
}

data class GameResult(
    val id: String,
    val gameType: GameType,
    val score: Int,
    val correct: Int,
    val total: Int,
    val durationMs: Long,
    val playedAt: Long,
)

sealed interface GameRound {
    val wordId: String

    data class Choice(
        override val wordId: String,
        val promptUdm: String,
        val promptEmoji: String,
        val options: List<String>,
        val correctIndex: Int,
    ) : GameRound

    data class OddOneOut(
        override val wordId: String,
        val oddWordId: String,
        val words: List<Word>,
        val oddIndex: Int,
    ) : GameRound

    data class Sorting(
        override val wordId: String,
        val items: List<SortItem>,
        val categoryNames: List<String>,
    ) : GameRound

    data class MemoryCard(
        override val wordId: String,
        val left: List<MemorySide>,
        val right: List<MemorySide>,
    ) : GameRound

    data class Builder(
        override val wordId: String,
        val udm: String,
        val ru: String,
        val emoji: String,
        val letters: List<String>,
    ) : GameRound

    data class TrueFalse(
        override val wordId: String,
        val udm: String,
        val ru: String,
        val emoji: String,
        val isTrue: Boolean,
    ) : GameRound
}

data class SortItem(
    val wordId: String,
    val label: String,
    val emoji: String,
    val categoryIndex: Int,
)

data class MemorySide(
    val wordId: String,
    val text: String,
    val isEmoji: Boolean,
)
