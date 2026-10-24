package org.udmurtlang.learn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.udmurtlang.learn.core.designsystem.ZecheburTheme
import org.udmurtlang.learn.feature.games.GamesHubScreen

class GamesHubScreenTest {

    @get:Rule val compose = createComposeRule()

    private val allGames = listOf(
        "Выбери перевод",
        "Найди лишнее слово",
        "Сортировка",
        "Найди пару",
        "Собери слово",
        "Правда или ложь",
    )

    @Test
    fun showsAllSixGames() {
        compose.setContent { ZecheburTheme { GamesHubScreen(onBack = {}, onOpenGame = {}) } }
        compose.onNodeWithText("Играсъёс").assertIsDisplayed()
        allGames.forEach { title ->
            compose.onNode(hasScrollAction()).performScrollToNode(hasText(title))
            compose.onNodeWithText(title).assertIsDisplayed()
        }
    }

    @Test
    fun tappingGameCallsCallbackWithSlug() {
        var slug = ""
        compose.setContent {
            ZecheburTheme { GamesHubScreen(onBack = {}, onOpenGame = { slug = it }) }
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Собери слово"))
        compose.onNodeWithText("Собери слово").performClick()
        assertEquals("word_builder", slug)
    }
}
