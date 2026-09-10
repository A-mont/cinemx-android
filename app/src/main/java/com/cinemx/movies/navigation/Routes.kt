package com.cinemx.movies.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object MovieListRoute

@Serializable
data class MovieDetailRoute(val movieId: Int)
