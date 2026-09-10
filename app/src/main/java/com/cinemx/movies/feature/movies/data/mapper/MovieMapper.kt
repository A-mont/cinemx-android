package com.cinemx.movies.feature.movies.data.mapper

import com.cinemx.movies.core.util.ImageUrls
import com.cinemx.movies.core.util.toRatingText
import com.cinemx.movies.core.util.toReleaseDateText
import com.cinemx.movies.core.util.toRuntimeText
import com.cinemx.movies.feature.movies.data.remote.MovieDetailDto
import com.cinemx.movies.feature.movies.data.remote.MovieDto
import com.cinemx.movies.feature.movies.domain.Movie
import com.cinemx.movies.feature.movies.domain.MovieDetail

private val CERTIFICATION_COUNTRIES = arrayOf("MX", "US")

fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title.ifBlank { originalTitle },
    posterUrl = ImageUrls.poster(posterPath),
    rating = voteAverage.toRatingText(),
    releaseDate = releaseDate.toReleaseDateText(),
)

fun MovieDetailDto.toDomain(): MovieDetail = MovieDetail(
    id = id,
    title = title,
    posterUrl = ImageUrls.posterLarge(posterPath),
    backdropUrl = ImageUrls.backdrop(backdropPath) ?: ImageUrls.posterLarge(posterPath),
    overview = overview.trim(),
    runtime = runtime.toRuntimeText(),
    releaseDate = releaseDate.toReleaseDateText(),
    certification = releaseDates.certificationFor(*CERTIFICATION_COUNTRIES),
    rating = voteAverage.toRatingText(),
    genres = genres.map { it.name }.filter { it.isNotBlank() },
)
