package com.cinemx.movies.feature.movies.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNowPlayingMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(genreId: Int? = null): Flow<PagingData<Movie>> =
        repository.getNowPlaying(genreId)
}

class GetMovieGenresUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(): Result<List<Genre>> = repository.getGenres()
}

class GetMovieDetailUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(movieId: Int): Result<MovieDetail> =
        repository.getMovieDetail(movieId)
}
