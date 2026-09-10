package com.cinemx.movies.feature.auth.domain

import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repository.login(email.trim(), password)
}

class LoginWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String, rawNonce: String?): Result<User> =
        repository.loginWithGoogleIdToken(idToken, rawNonce)
}

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
