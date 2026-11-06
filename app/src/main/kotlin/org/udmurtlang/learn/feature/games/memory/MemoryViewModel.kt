package org.udmurtlang.learn.feature.games.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.udmurtlang.learn.core.common.TimeProvider
import org.udmurtlang.learn.core.media.FeedbackPort
import org.udmurtlang.learn.core.media.WordSpeaker
import org.udmurtlang.learn.domain.game.GameRoundGenerator
import org.udmurtlang.learn.domain.model.GameType
import org.udmurtlang.learn.domain.model.MemorySide
import org.udmurtlang.learn.domain.usecase.GetGameWordsUseCase
import org.udmurtlang.learn.domain.usecase.MarkAnswerUseCase
import org.udmurtlang.learn.domain.usecase.SubmitGameResultUseCase
import org.udmurtlang.learn.feature.games.GameEffect
import org.udmurtlang.learn.feature.games.GameSession
import javax.inject.Inject
import kotlin.random.Random

data class MemoryState(
    val cards: List<MemorySide> = emptyList(),
    val matchedWordIds: Set<String> = emptySet(),
    val revealed: List<Int> = emptyList(),
    val moves: Int = 0,
    val finished: Boolean = false,
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    getWords: GetGameWordsUseCase,
    submit: SubmitGameResultUseCase,
    markAnswer: MarkAnswerUseCase,
    feedback: FeedbackPort,
    time: TimeProvider,
    private val speaker: WordSpeaker,
) : ViewModel() {

    private val generator = GameRoundGenerator(Random.Default)
    private val session = GameSession(GameType.MEMORY, submit, markAnswer, feedback, time)

    private val _state = MutableStateFlow(MemoryState())
    val state: StateFlow<MemoryState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val words = getWords(GameType.MEMORY, PAIR_COUNT)
            val round = generator.memory(words, PAIR_COUNT)
            val cards = round?.let { (it.left + it.right).shuffled() }.orEmpty()
            _state.update { it.copy(cards = cards, finished = cards.isEmpty()) }
            if (cards.isEmpty()) finish()
        }
    }

    fun reveal(index: Int) {
        val state = _state.value
        val card = state.cards.getOrNull(index) ?: return
        if (state.finished || state.revealed.size >= 2 ||
            index in state.revealed || card.wordId in state.matchedWordIds
        ) return

        val first = state.revealed.firstOrNull()?.let(state.cards::get)
        _state.update { it.copy(revealed = it.revealed + index, moves = it.moves + if (first == null) 0 else 1) }
        if (!card.isEmoji) speaker.speak(card.text)
        if (first == null) return

        val matched = first.wordId == card.wordId
        viewModelScope.launch {
            session.answer(if (first.isEmoji) card.wordId else first.wordId, matched)
            delay(if (matched) FEEDBACK_MS else WRONG_MS)
            val matchedIds = if (matched) state.matchedWordIds + card.wordId else state.matchedWordIds
            val finished = matchedIds.size * 2 == state.cards.size
            _state.update { it.copy(matchedWordIds = matchedIds, revealed = emptyList(), finished = finished) }
            if (finished) finish()
        }
    }

    private fun finish() {
        viewModelScope.launch {
            session.victory()
            val total = _state.value.cards.size / 2
            val result = session.finish(total)
            _effects.send(
                GameEffect.Finished(
                    slug = GameType.MEMORY.slug,
                    score = result.score,
                    correct = result.correct,
                    total = total,
                ),
            )
        }
    }

    companion object {
        const val PAIR_COUNT = 6
        private const val FEEDBACK_MS = 450L
        private const val WRONG_MS = 700L
    }
}
