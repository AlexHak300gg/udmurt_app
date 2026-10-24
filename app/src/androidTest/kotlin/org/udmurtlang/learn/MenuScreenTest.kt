package org.udmurtlang.learn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.udmurtlang.learn.core.designsystem.ZecheburTheme
import org.udmurtlang.learn.feature.menu.MenuScreen

@HiltAndroidTest
class MenuScreenTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsGreetingAndFourSections() {
        compose.setContent {
            ZecheburTheme {
                MenuScreen(
                    onOpenLearning = {},
                    onOpenGames = {},
                    onOpenProfile = {},
                    onOpenSettings = {},
                    onOpenWordOfDay = {},
                )
            }
        }
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("Обучение").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Обучение").assertIsDisplayed()
        compose.onNodeWithText("Игры").assertIsDisplayed()
        compose.onNodeWithText("Профиль").assertIsDisplayed()
        compose.onNodeWithText("Настройки").assertIsDisplayed()
        compose.onNodeWithText("Слово дня").assertExists()
    }

    @Test
    fun sectionClicksReachCallbacks() {
        var opened = ""
        compose.setContent {
            ZecheburTheme {
                MenuScreen(
                    onOpenLearning = { opened = "learning" },
                    onOpenGames = { opened = "games" },
                    onOpenProfile = {},
                    onOpenSettings = {},
                    onOpenWordOfDay = {},
                )
            }
        }
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("Игры").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Обучение").performClick()
        assertTrue(opened == "learning")
        compose.onNodeWithText("Игры").performClick()
        assertTrue(opened == "games")
    }
}
