package com.cinemx.movies.feature.movies.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cinemx.movies.core.network.NetworkException
import com.cinemx.movies.core.network.toNetworkError
import com.cinemx.movies.feature.movies.data.mapper.toDomain
import com.cinemx.movies.feature.movies.domain.Movie
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** `total_pages` marca el final: `nextKey` es `null` ahí, en vez de pedir una página vacía. */
class NowPlayingPagingSource(
    private val api: TmdbApi,
    private val genreId: Int? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = request(page)
            LoadResult.Page(
                data = response.results.map { it.toDomain() },
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (page >= response.totalPages) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Se traduce a dominio para que la UI muestre un mensaje propio.
            LoadResult.Error(NetworkException(e.toNetworkError()))
        }
    }

    private suspend fun request(page: Int): PagedResponseDto<MovieDto> {
        if (genreId == null) return api.getNowPlaying(page = page)

        val today = LocalDate.now(clock)
        return api.discoverByGenre(
            page = page,
            genreId = genreId,
            releasedFrom = today.minusDays(TmdbApi.NOW_PLAYING_WINDOW_DAYS).format(API_DATE),
            releasedTo = today.format(API_DATE),
        )
    }

    /** Reanuda en la página del ítem ancla, retrocediendo una para no dejar huecos. */
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null
        return closestPage.prevKey?.plus(1) ?: closestPage.nextKey?.minus(1)
    }

    private companion object {
        const val FIRST_PAGE = 1
        val API_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
