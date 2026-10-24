package org.udmurtlang.learn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.ZecheburTheme
import org.udmurtlang.learn.core.designsystem.components.ChoiceTile
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.TileState
import org.udmurtlang.learn.core.designsystem.components.WordCard
import org.udmurtlang.learn.domain.model.Word

class DesignSystemTest {

    @get:Rule val compose = createComposeRule()

    @Test
    fun gameButtonShowsTextAndHandlesClick() {
        var clicks = 0
        compose.setContent {
            ZecheburTheme {
                GameButton(text = "Начать", onClick = { clicks++ })
            }
        }
        compose.onNodeWithText("Начать").assertIsDisplayed()
        compose.onNodeWithText("Начать").performClick()
        compose.onNodeWithText("Начать").performClick()
        assertEquals(2, clicks)
    }

    @Test
    fun choiceTileInCorrectStateIsGreenAndClickable() {
        var picked = ""
        compose.setContent {
            ZecheburTheme {
                ChoiceTile(
                    label = "Скала",
                    state = TileState.CORRECT,
                    onClick = { picked = "скала" },
                )
            }
        }
        compose.onNodeWithText("Скала").assertIsDisplayed()
        compose.onNodeWithText("Скала").performClick()
        assertEquals("скала", picked)
    }

    @Test
    fun wordCardRendersWordAndInvokesSpeak() {
        var spoken = ""
        val word = Word(
            id = "w1",
            topicId = "t1",
            udm = "Ӟечбур",
            ru = "здравствуй",
            transcription = "[ҷечбур]",
            emoji = "👋",
        )
        compose.setContent {
            ZecheburTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("word_card_container"),
                ) {
                    WordCard(word = word, onSpeak = { spoken = word.udm })
                }
            }
        }
        compose.onNodeWithText("Ӟечбур").assertIsDisplayed()
        compose.onNodeWithText("(Перевод)").assertIsDisplayed()
        compose.onNodeWithText("здравствуй").assertIsDisplayed()
        compose.onNodeWithText("(ҷечбур)").assertIsDisplayed()
        compose.onNodeWithContentDescription("Прослушать").performClick()
        assertEquals("Ӟечбур", spoken)
    }

    @Test
    fun wordCardWithoutSpeakHidesButton() {
        val word = Word("w1", "t1", "Тау", "спасибо", "[тау]", "🙏")
        compose.setContent {
            ZecheburTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("word_card_container_2"),
                ) {
                    WordCard(word = word, onSpeak = null)
                }
            }
        }
        compose.onNodeWithText("Тау").assertIsDisplayed()
        compose.onNodeWithContentDescription("Прослушать").assertDoesNotExist()
    }

    @Test
    fun ornamentBandRenders() {
        compose.setContent {
            ZecheburTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ornament_container"),
                ) {
                    OrnamentBand()
                }
            }
        }
        assertTrue(compose.onNodeWithTag("ornament_container").fetchSemanticsNode() != null)
    }
}
