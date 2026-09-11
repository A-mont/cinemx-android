package com.cinemx.movies.feature.movies.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cinemx.movies.core.network.safeApiCall
import com.cinemx.movies.feature.movies.data.mapper.toDomain
import com.cinemx.movies.feature.movies.data.remote.NowPlayingPagingSource
import com.cinemx.movies.feature.movies.data.remote.TmdbApi
import com.cinemx.movies.feature.movies.domain.Genre
import com.cinemx.movies.feature.movies.domain.Movie
import com.cinemx.movies.feature.movies.domain.MovieDetail
import com.cinemx.movies.feature.movies.domain.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
) : MovieRepository {

    @Volatile
    private var cachedGenres: List<Genre>? = null

    override fun getNowPlaying(genreId: Int?): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(
            // TMDB devuelve 20 ítems por página; declararlo evita que Paging
            // pida varias páginas de golpe en el primer load.
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { NowPlayingPagingSource(api, genreId) },
    ).flow

    /** El catálogo de TMDB cambia una o dos veces al año: basta con pedirlo una vez. */
    override suspend fun getGenres(): Result<List<Genre>> {
        cachedGenres?.let { return Result.success(it) }

        return safeApiCall {
            api.getGenres().genres
                .filter { it.name.isNotBlank() }
                .map { Genre(id = it.id, name = it.name) }
        }.onSuccess { cachedGenres = it }
    }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail> = safeApiCall {
        api.getMovieDetail(movieId).toDomain()
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val PREFETCH_DISTANCE = 5
    }
}
