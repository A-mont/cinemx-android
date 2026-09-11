package com.cinemx.movies.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object MovieListRoute

@Serializable
data class MovieDetailRoute(val movieId: Int) {
    companion object {
        // Navigation guarda cada argumento en el SavedStateHandle bajo el nombre
        // de su propiedad. Se expone aquí para leerlo sin depender de `toRoute()`,
        // que necesita un Bundle real y no funciona en tests JVM.
        const val MOVIE_ID_ARG = "movieId"
    }
}
