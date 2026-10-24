package org.udmurtlang.learn.feature.games

import androidx.compose.runtime.Composable
import org.udmurtlang.learn.feature.games.builder.BuilderScreen
import org.udmurtlang.learn.feature.games.choice.ChoiceGameScreen
import org.udmurtlang.learn.feature.games.memory.MemoryScreen
import org.udmurtlang.learn.feature.games.oddoneout.OddOneOutScreen
import org.udmurtlang.learn.feature.games.sorting.SortingScreen
import org.udmurtlang.learn.feature.games.truefalse.TrueFalseScreen
import org.udmurtlang.learn.domain.model.GameType

@Composable
fun GameRoute(
    slug: String,
    onFinished: (String, Int, Int, Int) -> Unit,
    onBack: () -> Unit,
) {
    val gameType = GameType.entries.find { it.slug == slug } ?: GameType.CHOICE
    val finish: (Int, Int, Int) -> Unit = { score, correct, total ->
        onFinished(gameType.slug, score, correct, total)
    }
    when (gameType) {
        GameType.CHOICE -> ChoiceGameScreen(onFinished = finish, onBack = onBack)
        GameType.ODD_ONE_OUT -> OddOneOutScreen(onFinished = finish, onBack = onBack)
        GameType.SORTING -> SortingScreen(onFinished = finish, onBack = onBack)
        GameType.MEMORY -> MemoryScreen(onFinished = finish, onBack = onBack)
        GameType.WORD_BUILDER -> BuilderScreen(onFinished = finish, onBack = onBack)
        GameType.TRUE_FALSE -> TrueFalseScreen(onFinished = finish, onBack = onBack)
    }
}
