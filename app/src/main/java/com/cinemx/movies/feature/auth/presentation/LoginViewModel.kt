package com.cinemx.movies.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemx.movies.R
import com.cinemx.movies.core.util.UiText
import com.cinemx.movies.feature.auth.domain.AuthError
import com.cinemx.movies.feature.auth.domain.AuthException
import com.cinemx.movies.feature.auth.domain.LoginUseCase
import com.cinemx.movies.feature.auth.domain.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: LoginUseCase,
    private val loginWithGoogle: LoginWithGoogleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChange(value: String) {
        // El error se limpia al escribir: corregir el campo no debe seguir mostrando el fallo.
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /** Valida en local antes de llamar a Supabase: ahorra la petición y responde antes. */
    fun onSubmit() {
        val state = _uiState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = login(state.email, state.password)
            _uiState.update { it.copy(isLoading = false) }
            result.emitOutcome()
        }
    }

    fun onGoogleToken(idToken: String, rawNonce: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }
            val result = loginWithGoogle(idToken, rawNonce)
            _uiState.update { it.copy(isGoogleLoading = false) }
            result.emitOutcome()
        }
    }

    fun onGoogleFailed(throwable: Throwable) {
        _uiState.update { it.copy(isGoogleLoading = false) }
        viewModelScope.launch {
            _events.send(LoginEvent.ShowError(throwable.toMessage()))
        }
    }

    fun onGoogleStarted() {
        _uiState.update { it.copy(isGoogleLoading = true) }
    }

    private suspend fun Result<*>.emitOutcome() {
        onSuccess { _events.send(LoginEvent.NavigateToHome) }
        onFailure { _events.send(LoginEvent.ShowError(it.toMessage())) }
    }

    private fun validateEmail(email: String): UiText? = when {
        email.isBlank() -> UiText.of(R.string.login_error_email_blank)
        !EMAIL_REGEX.matches(email.trim()) -> UiText.of(R.string.login_error_email_invalid)
        else -> null
    }

    private fun validatePassword(password: String): UiText? = when {
        password.isBlank() -> UiText.of(R.string.login_error_password_blank)
        password.length < MIN_PASSWORD_LENGTH -> UiText.of(R.string.login_error_password_short)
        else -> null
    }

    private fun Throwable.toMessage(): UiText = when ((this as? AuthException)?.error) {
        AuthError.InvalidCredentials -> UiText.of(R.string.login_error_invalid_credentials)
        AuthError.EmailNotConfirmed -> UiText.of(R.string.login_error_email_not_confirmed)
        AuthError.TooManyRequests -> UiText.of(R.string.login_error_too_many_requests)
        AuthError.NoConnection -> UiText.of(R.string.error_no_connection)
        AuthError.GoogleCancelled -> UiText.of(R.string.login_error_google_cancelled)
        AuthError.GoogleUnavailable -> UiText.of(R.string.login_error_google_unavailable)
        else -> UiText.of(R.string.error_unknown)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6

        /** Regex y no `android.util.Patterns`, que en la JVM de tests devuelve `null`. */
        val EMAIL_REGEX = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
    }
}
