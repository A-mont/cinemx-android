package com.cinemx.movies.feature.auth.domain

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
) {
    /** `full_name` del proveedor si existe; si no, la parte local del correo. */
    val greetingName: String
        get() = displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore('@').takeIf { it.isNotBlank() }
            ?: email
}
