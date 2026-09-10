package com.cinemx.movies.feature.movies.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun getNowPlaying(): Flow<PagingData<Movie>>

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail>
}
