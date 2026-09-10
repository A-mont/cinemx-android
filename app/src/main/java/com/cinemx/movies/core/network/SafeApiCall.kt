package com.cinemx.movies.core.network

import kotlinx.coroutines.CancellationException

/** `CancellationException` se relanza: un `catch (e: Exception)` a secas rompería la cancelación. */
suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(NetworkException(e.toNetworkError()))
    }
