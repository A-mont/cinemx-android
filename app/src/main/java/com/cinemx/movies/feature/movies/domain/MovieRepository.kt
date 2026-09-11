package com.cinemx.movies.feature.movies.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun getNowPlaying(genreId: Int? = null): Flow<PagingData<Movie>>

    suspend fun getGenres(): Result<List<Genre>>

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail>
}
