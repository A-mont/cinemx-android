package com.cinemx.movies.feature.movies.data.remote

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cinemx.movies.core.network.NetworkError
import com.cinemx.movies.core.network.NetworkException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class NowPlayingPagingSourceTest {

    private val api = mockk<TmdbApi>()
    private val config = PagingConfig(pageSize = 20, enablePlaceholders = false)

    @Test
    fun `la primera carga devuelve los items mapeados`() = runTest {
        coEvery { api.getNowPlaying(page = 1, region = any()) } returns page(1, totalPages = 3)

        val pager = TestPager(config, NowPlayingPagingSource(api))
        val result = pager.refresh() as PagingSource.LoadResult.Page

        assertEquals(2, result.data.size)
        assertEquals("Película 1", result.data.first().title)
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)
    }

    @Test
    fun `nextKey es null en la ultima pagina`() = runTest {
        coEvery { api.getNowPlaying(page = 1, region = any()) } returns page(1, totalPages = 1)

        val pager = TestPager(config, NowPlayingPagingSource(api))
        val result = pager.refresh() as PagingSource.LoadResult.Page

        assertNull(result.nextKey)
    }

    @Test
    fun `encadena paginas al hacer append`() = runTest {
        coEvery { api.getNowPlaying(page = 1, region = any()) } returns page(1, totalPages = 2)
        coEvery { api.getNowPlaying(page = 2, region = any()) } returns page(2, totalPages = 2)

        val pager = TestPager(config, NowPlayingPagingSource(api))
        pager.refresh()
        val second = pager.append() as PagingSource.LoadResult.Page

        assertEquals(1, second.prevKey)
        assertNull(second.nextKey)
        assertEquals("Película 3", second.data.first().title)
    }

    @Test
    fun `traduce la excepcion de red a NetworkError`() = runTest {
        coEvery { api.getNowPlaying(page = 1, region = any()) } throws IOException("sin red")

        val pager = TestPager(config, NowPlayingPagingSource(api))
        val result = pager.refresh() as PagingSource.LoadResult.Error

        val error = result.throwable
        assertTrue(error is NetworkException)
        assertEquals(NetworkError.NoConnection, (error as NetworkException).error)
    }

    @Test
    fun `con genero pide discover con la ventana de cartelera`() = runTest {
        val clock = Clock.fixed(Instant.parse("2026-09-11T10:00:00Z"), ZoneOffset.UTC)
        coEvery {
            api.discoverByGenre(
                page = 1,
                genreId = 28,
                releasedFrom = any(),
                releasedTo = any(),
                region = any(),
                sortBy = any(),
                releaseType = any(),
            )
        } returns page(1, totalPages = 1)

        val source = NowPlayingPagingSource(api, genreId = 28, clock = clock)
        val result = TestPager(config, source).refresh() as PagingSource.LoadResult.Page

        assertEquals(2, result.data.size)
        coVerify {
            api.discoverByGenre(
                page = 1,
                genreId = 28,
                releasedFrom = "2026-07-28",
                releasedTo = "2026-09-11",
                region = any(),
                sortBy = any(),
                releaseType = any(),
            )
        }
        coVerify(exactly = 0) { api.getNowPlaying(any(), any()) }
    }

    private fun page(page: Int, totalPages: Int): PagedResponseDto<MovieDto> {
        val firstIndex = (page - 1) * 2 + 1
        return PagedResponseDto(
            page = page,
            totalPages = totalPages,
            totalResults = totalPages * 2,
            results = listOf(firstIndex, firstIndex + 1).map { index ->
                MovieDto(
                    id = index,
                    title = "Película $index",
                    voteAverage = 7.5,
                    posterPath = "/poster$index.jpg",
                    releaseDate = "2026-09-12",
                )
            },
        )
    }
}
