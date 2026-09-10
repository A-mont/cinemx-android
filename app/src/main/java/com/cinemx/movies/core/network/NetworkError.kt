package com.cinemx.movies.core.network

import com.cinemx.movies.R
import com.cinemx.movies.core.util.UiText

sealed interface NetworkError {
    data object NoConnection : NetworkError
    data object Unauthorized : NetworkError
    data object NotFound : NetworkError
    data object Server : NetworkError
    data class Unknown(val cause: Throwable) : NetworkError
}

class NetworkException(val error: NetworkError) : Exception(error.toString())

fun NetworkError.toUiText(): UiText = when (this) {
    NetworkError.NoConnection -> UiText.of(R.string.error_no_connection)
    NetworkError.Unauthorized -> UiText.of(R.string.error_unauthorized)
    NetworkError.NotFound -> UiText.of(R.string.error_not_found)
    NetworkError.Server -> UiText.of(R.string.error_server)
    is NetworkError.Unknown -> UiText.of(R.string.error_unknown)
}

fun Throwable.toNetworkError(): NetworkError = when (this) {
    is NetworkException -> error
    is java.io.IOException -> NetworkError.NoConnection
    is retrofit2.HttpException -> when (code()) {
        HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> NetworkError.Unauthorized
        HTTP_NOT_FOUND -> NetworkError.NotFound
        in HTTP_SERVER_ERROR_RANGE -> NetworkError.Server
        else -> NetworkError.Unknown(this)
    }
    else -> NetworkError.Unknown(this)
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private val HTTP_SERVER_ERROR_RANGE = 500..599
