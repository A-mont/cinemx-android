package com.cinemx.movies.feature.auth.presentation

import com.cinemx.movies.core.util.UiText

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
) {
    /** Bloqueado durante la carga, para evitar envíos duplicados. */
    val isSubmitEnabled: Boolean
        get() = !isLoading && !isGoogleLoading
}

/** Eventos de una sola vez: fuera del estado, para que no se repitan al recomponer. */
sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
    data class ShowError(val message: UiText) : LoginEvent
}
