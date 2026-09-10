package com.cinemx.movies.feature.auth.presentation

import app.cash.turbine.test
import com.cinemx.movies.MainDispatcherRule
import com.cinemx.movies.R
import com.cinemx.movies.core.util.UiText
import com.cinemx.movies.feature.auth.domain.AuthError
import com.cinemx.movies.feature.auth.domain.AuthException
import com.cinemx.movies.feature.auth.domain.AuthRepository
import com.cinemx.movies.feature.auth.domain.LoginUseCase
import com.cinemx.movies.feature.auth.domain.LoginWithGoogleUseCase
import com.cinemx.movies.feature.auth.domain.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<AuthRepository>(relaxed = true)

    private fun createViewModel() = LoginViewModel(
        login = LoginUseCase(repository),
        loginWithGoogle = LoginWithGoogleUseCase(repository),
    )

    @Test
    fun `no llama al repositorio cuando el correo no es valido`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChange("no-es-un-correo")
        viewModel.onPasswordChange("123456")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            UiText.of(R.string.login_error_email_invalid),
            viewModel.uiState.value.emailError,
        )
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `marca error cuando la contrasena es demasiado corta`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChange("usuario@cinemx.app")
        viewModel.onPasswordChange("123")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            UiText.of(R.string.login_error_password_short),
            viewModel.uiState.value.passwordError,
        )
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `limpia el error del campo al volver a escribir`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChange("malo")
        viewModel.onPasswordChange("123456")
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.onEmailChange("usuario@cinemx.app")

        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `navega a Home cuando las credenciales son correctas`() = runTest {
        coEvery { repository.login(EMAIL, PASSWORD) } returns Result.success(user)
        val viewModel = createViewModel()
        viewModel.onEmailChange(EMAIL)
        viewModel.onPasswordChange(PASSWORD)

        viewModel.events.test {
            viewModel.onSubmit()
            advanceUntilIdle()

            assertEquals(LoginEvent.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `traduce credenciales invalidas a un mensaje claro y libera el boton`() = runTest {
        coEvery { repository.login(EMAIL, PASSWORD) } returns
            Result.failure(AuthException(AuthError.InvalidCredentials))
        val viewModel = createViewModel()
        viewModel.onEmailChange(EMAIL)
        viewModel.onPasswordChange(PASSWORD)

        viewModel.events.test {
            viewModel.onSubmit()
            advanceUntilIdle()

            assertEquals(
                LoginEvent.ShowError(UiText.of(R.string.login_error_invalid_credentials)),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `traduce la falta de red a su propio mensaje`() = runTest {
        coEvery { repository.login(EMAIL, PASSWORD) } returns
            Result.failure(AuthException(AuthError.NoConnection))
        val viewModel = createViewModel()
        viewModel.onEmailChange(EMAIL)
        viewModel.onPasswordChange(PASSWORD)

        viewModel.events.test {
            viewModel.onSubmit()
            advanceUntilIdle()

            assertEquals(
                LoginEvent.ShowError(UiText.of(R.string.error_no_connection)),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `el boton se bloquea mientras hay una peticion en curso`() = runTest {
        // En vuelo hasta completar el Deferred, para observar el estado intermedio.
        val inFlight = CompletableDeferred<Result<User>>()
        coEvery { repository.login(EMAIL, PASSWORD) } coAnswers { inFlight.await() }

        val viewModel = createViewModel()
        viewModel.onEmailChange(EMAIL)
        viewModel.onPasswordChange(PASSWORD)

        viewModel.onSubmit()
        runCurrent()
        assertFalse(viewModel.uiState.value.isSubmitEnabled)

        inFlight.complete(Result.success(user))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSubmitEnabled)
    }

    private companion object {
        const val EMAIL = "usuario@cinemx.app"
        const val PASSWORD = "CineMX2026"

        val user = User(id = "uuid", email = EMAIL, displayName = null)
    }
}
