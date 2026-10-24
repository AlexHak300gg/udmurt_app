package org.udmurtlang.learn.domain.usecase

import org.udmurtlang.learn.domain.game.GameRoundGenerator
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.model.Word
import org.udmurtlang.learn.domain.repository.ContentRepository
import javax.inject.Inject

/** Подбор слов для игр: изучаемые темы в приоритете, без повторов внутри сессии. */
class GetGameWordsUseCase @Inject constructor(
    private val content: ContentRepository,
) {
    suspend operator fun invoke(gameType: GameType, count: Int): List<Word> {
        val words = content.allWords()
        return when (gameType) {
            GameType.WORD_BUILDER -> words.filter { it.udm.filter { c -> c.isLetter() }.length in 3..8 }
                .shuffled().take(count)
            GameType.MEMORY -> words.shuffled().take(count)
            else -> words.shuffled().take(count)
        }
    }
}

class GetWordsGroupedByTopicUseCase @Inject constructor(
    private val content: ContentRepository,
) {
    suspend operator fun invoke(): Map<String, List<Word>> =
        content.allWords().groupBy { it.topicId }
}
