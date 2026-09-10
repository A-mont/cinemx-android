package com.cinemx.movies.feature.movies.domain

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val rating: String?,
    val releaseDate: String?,
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val overview: String,
    val runtime: String?,
    val releaseDate: String?,
    val certification: String?,
    val rating: String?,
    val genres: List<String>,
)
