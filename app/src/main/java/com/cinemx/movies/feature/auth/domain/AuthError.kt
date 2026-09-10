package com.cinemx.movies.feature.auth.domain

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object EmailNotConfirmed : AuthError
    data object TooManyRequests : AuthError
    data object NoConnection : AuthError
    data object GoogleCancelled : AuthError
    data object GoogleUnavailable : AuthError
    data class Unknown(val cause: Throwable?) : AuthError
}

class AuthException(val error: AuthError) : Exception(error.toString())
