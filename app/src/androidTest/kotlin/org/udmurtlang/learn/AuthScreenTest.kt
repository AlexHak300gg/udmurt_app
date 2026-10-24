package org.udmurtlang.learn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.udmurtlang.learn.core.designsystem.ZecheburTheme
import org.udmurtlang.learn.feature.auth.AuthScreen

@HiltAndroidTest
class AuthScreenTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun formAndGuestButtonAreVisible() {
        compose.setContent { ZecheburTheme { AuthScreen(onAuthenticated = {}) } }
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("Войти").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("E-mail").assertIsDisplayed()
        compose.onNodeWithText("Пароль").assertIsDisplayed()
        compose.onNodeWithText("Войти").assertIsDisplayed()
        compose.onNodeWithText("Играть без регистрации").assertIsDisplayed()
    }

    @Test
    fun emptyFormShowsValidationError() {
        compose.setContent { ZecheburTheme { AuthScreen(onAuthenticated = {}) } }
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("Войти").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Войти").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Нужен e-mail в формате name@mail.ru")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
