package com.cinemx.movies.feature.auth.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.cinemx.movies.BuildConfig
import com.cinemx.movies.feature.auth.domain.AuthError
import com.cinemx.movies.feature.auth.domain.AuthException
import kotlinx.coroutines.CancellationException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleIdTokenResult(
    val idToken: String,
    val rawNonce: String,
)

/**
 * El nonce viaja doble a propósito: a Google el **hash SHA-256**, a Supabase el valor
 * **en claro**. Supabase lo re-hashea y lo compara con el del token, de modo que este
 * no puede reutilizarse en otra sesión.
 */
@Singleton
class GoogleIdTokenProvider @Inject constructor() {

    /** @param activityContext debe ser el de la Activity: hace falta una ventana. */
    suspend fun requestIdToken(activityContext: Context): Result<GoogleIdTokenResult> {
        val rawNonce = generateRawNonce()

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            // false = permite elegir cualquier cuenta, no solo las ya autorizadas.
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setNonce(rawNonce.sha256Hex())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)

            val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
            Result.success(GoogleIdTokenResult(credential.idToken, rawNonce))
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Result.failure(AuthException(AuthError.GoogleCancelled))
        } catch (e: NoCredentialException) {
            Result.failure(AuthException(AuthError.GoogleUnavailable))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthError.Unknown(e)))
        }
    }

    private fun generateRawNonce(): String {
        val bytes = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }
        return bytes.toHex()
    }

    private fun String.sha256Hex(): String =
        MessageDigest.getInstance("SHA-256").digest(toByteArray()).toHex()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private companion object {
        const val NONCE_BYTES = 32
    }
}
