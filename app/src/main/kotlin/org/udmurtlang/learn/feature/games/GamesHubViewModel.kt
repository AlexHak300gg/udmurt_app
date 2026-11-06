package org.udmurtlang.learn.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.udmurtlang.learn.domain.game.GameScoring
import org.udmurtlang.learn.domain.repository.GameRepository
import javax.inject.Inject

@HiltViewModel
class GamesHubViewModel @Inject constructor(repository: GameRepository) : ViewModel() {
    val starsByGame = repository.observeResults().map { results ->
        results.groupBy { it.gameType }.mapValues { (_, games) ->
            games.sumOf { GameScoring.stars(it.correct, it.total) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
