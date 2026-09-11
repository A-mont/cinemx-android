package com.cinemx.movies.feature.movies.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PagedResponseDto<T>(
    @SerialName("page") val page: Int = 1,
    @SerialName("results") val results: List<T> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class MovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("original_title") val originalTitle: String = "",
    @SerialName("overview") val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("release_date") val releaseDate: String? = null,
)

@Serializable
data class MovieDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("overview") val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    // Llega solo cuando se pide con `append_to_response=release_dates`.
    @SerialName("release_dates") val releaseDates: ReleaseDatesDto? = null,
)

@Serializable
data class GenresResponseDto(
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
)

@Serializable
data class GenreDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
)

@Serializable
data class ReleaseDatesDto(
    @SerialName("results") val results: List<CountryReleaseDatesDto> = emptyList(),
)

@Serializable
data class CountryReleaseDatesDto(
    @SerialName("iso_3166_1") val iso31661: String = "",
    @SerialName("release_dates") val releaseDates: List<ReleaseDateDto> = emptyList(),
)

@Serializable
data class ReleaseDateDto(
    @SerialName("certification") val certification: String = "",
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("type") val type: Int = 0,
)
