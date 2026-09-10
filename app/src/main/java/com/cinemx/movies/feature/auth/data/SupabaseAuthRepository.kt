package com.cinemx.movies.feature.auth.data

import com.cinemx.movies.feature.auth.domain.AuthError
import com.cinemx.movies.feature.auth.domain.AuthException
import com.cinemx.movies.feature.auth.domain.AuthRepository
import com.cinemx.movies.feature.auth.domain.SessionState
import com.cinemx.movies.feature.auth.domain.User
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase persiste la sesión y refresca el token por su cuenta; aquí solo se traduce
 * su `sessionStatus` al estado de dominio y se normalizan los errores.
 */
@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val auth: Auth,
) : AuthRepository {

    override val sessionState: Flow<SessionState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user
                ?.let { SessionState.Authenticated(it.toDomain()) }
                ?: SessionState.Unauthenticated

            is SessionStatus.NotAuthenticated -> SessionState.Unauthenticated

            // Cuando el refresh falla se trata como sesión caducada: el usuario
            // vuelve al login en vez de quedarse en una pantalla que no carga.
            is SessionStatus.RefreshFailure -> SessionState.Unauthenticated

            else -> SessionState.Resolving
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = runAuth {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        currentUserOrThrow()
    }

    override suspend fun loginWithGoogleIdToken(idToken: String, rawNonce: String?): Result<User> =
        runAuth {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
                this.nonce = rawNonce
            }
            currentUserOrThrow()
        }

    override suspend fun logout(): Result<Unit> = runAuth {
        auth.signOut()
    }

    private fun currentUserOrThrow(): User =
        auth.currentUserOrNull()?.toDomain()
            ?: throw AuthException(AuthError.Unknown(null))

    /** `CancellationException` se relanza para respetar la cancelación estructurada. */
    private inline fun <T> runAuth(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(AuthException(e.toAuthError()))
    }
}

private fun UserInfo.toDomain(): User = User(
    id = id,
    email = email.orEmpty(),
    // Supabase guarda el nombre del proveedor OAuth en user_metadata.
    displayName = userMetadata.metadataString("full_name") ?: userMetadata.metadataString("name"),
)

private fun JsonObject?.metadataString(key: String): String? =
    (this?.get(key) as? JsonPrimitive)
        ?.content
        ?.takeIf { it.isNotBlank() && it != "null" }

/**
 * Se compara contra el cuerpo del error y no contra un enum de códigos: esos códigos han
 * cambiado entre versiones de supabase-kt, mientras que el mensaje se ha mantenido estable.
 */
private fun Throwable.toAuthError(): AuthError {
    if (this is AuthException) return error
    if (this is HttpRequestException || this is IOException) return AuthError.NoConnection

    val body = (message ?: "").lowercase()
    val status = (this as? RestException)?.statusCode

    return when {
        "invalid login credentials" in body || "invalid_credentials" in body ->
            AuthError.InvalidCredentials

        "email not confirmed" in body || "email_not_confirmed" in body ->
            AuthError.EmailNotConfirmed

        status == HTTP_TOO_MANY_REQUESTS || "over_request_rate_limit" in body ->
            AuthError.TooManyRequests

        status == HTTP_BAD_REQUEST || status == HTTP_UNAUTHORIZED ->
            AuthError.InvalidCredentials

        else -> AuthError.Unknown(this)
    }
}

private const val HTTP_BAD_REQUEST = 400
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_TOO_MANY_REQUESTS = 429
