package com.cinemx.movies.feature.auth.domain

import kotlinx.coroutines.flow.Flow

sealed interface SessionState {
    /** Aún no se sabe: el splash sigue visible para evitar el parpadeo del login. */
    data object Resolving : SessionState
    data class Authenticated(val user: User) : SessionState
    data object Unauthenticated : SessionState
}

/** Sin mencionar a Supabase: cambiar de proveedor es otra implementación de esta interfaz. */
interface AuthRepository {

    val sessionState: Flow<SessionState>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun loginWithGoogleIdToken(idToken: String, rawNonce: String?): Result<User>

    suspend fun logout(): Result<Unit>
}
