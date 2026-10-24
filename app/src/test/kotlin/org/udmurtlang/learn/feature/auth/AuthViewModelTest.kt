package org.udmurtlang.learn.feature.auth

import app.cash.turbine.test
import org.udmurtlang.learn.data.supabase.SupabaseProvider
import org.udmurtlang.learn.domain.usecase.ContinueAsGuestUseCase
import org.udmurtlang.learn.domain.usecase.LoginUseCase
import org.udmurtlang.learn.domain.usecase.RegisterUseCase
import org.udmurtlang.learn.testing.FakeAccountRepository
import org.udmurtlang.learn.testing.runVmTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthViewModelTest {

    private fun viewModel(account: FakeAccountRepository = FakeAccountRepository()) =
        AuthViewModel(
            register = RegisterUseCase(account),
            login = LoginUseCase(account),
            guest = ContinueAsGuestUseCase(account),
            cloudModeProvider = SupabaseProvider(),
        )

    @Test
    fun `invalid email shows error and does not call repository`() = runVmTest {
        val account = FakeAccountRepository()
        val vm = viewModel(account)
        vm.onIntent(AuthIntent.EmailChanged("not-an-email"))
        vm.onIntent(AuthIntent.PasswordChanged("123456"))
        vm.onIntent(AuthIntent.Submit)
        assertEquals("auth_error_email", vm.state.value.error)
        assertEquals(0, account.loginCalls)
    }

    @Test
    fun `short password shows password error`() = runVmTest {
        val vm = viewModel()
        vm.onIntent(AuthIntent.EmailChanged("kid@example.com"))
        vm.onIntent(AuthIntent.PasswordChanged("123"))
        vm.onIntent(AuthIntent.Submit)
        assertEquals("auth_error_password", vm.state.value.error)
    }

    @Test
    fun `register without name shows name error`() = runVmTest {
        val vm = viewModel()
        vm.onIntent(AuthIntent.ToggleMode)
        vm.onIntent(AuthIntent.EmailChanged("kid@example.com"))
        vm.onIntent(AuthIntent.PasswordChanged("123456"))
        vm.onIntent(AuthIntent.Submit)
        assertEquals("auth_error_name", vm.state.value.error)
    }

    @Test
    fun `successful login emits authenticated`() = runVmTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(AuthIntent.EmailChanged("kid@example.com"))
            vm.onIntent(AuthIntent.PasswordChanged("123456"))
            vm.onIntent(AuthIntent.Submit)
            assertEquals(AuthEffect.Authenticated, awaitItem())
            assertTrue(!vm.state.value.isLoading)
            assertNull(vm.state.value.error)
        }
    }

    @Test
    fun `failed login surfaces repository message`() = runVmTest {
        val account = FakeAccountRepository().apply { failWith = "Неверная пара" }
        val vm = viewModel(account)
        vm.onIntent(AuthIntent.EmailChanged("kid@example.com"))
        vm.onIntent(AuthIntent.PasswordChanged("123456"))
        vm.onIntent(AuthIntent.Submit)
        assertEquals("Неверная пара", vm.state.value.error)
    }

    @Test
    fun `guest mode emits authenticated`() = runVmTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(AuthIntent.ContinueAsGuest)
            assertEquals(AuthEffect.Authenticated, awaitItem())
        }
    }

    @Test
    fun `forgot password in local mode shows local message`() = runVmTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(AuthIntent.ForgotPassword)
            assertEquals(AuthEffect.ShowMessage("auth_forgot_local"), awaitItem())
        }
    }
}
