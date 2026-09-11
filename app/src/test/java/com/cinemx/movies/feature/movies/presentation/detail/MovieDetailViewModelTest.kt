package com.cinemx.movies.feature.movies.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cinemx.movies.MainDispatcherRule
import com.cinemx.movies.core.network.NetworkError
import com.cinemx.movies.core.network.NetworkException
import com.cinemx.movies.feature.movies.domain.GetMovieDetailUseCase
import com.cinemx.movies.feature.movies.domain.MovieDetail
import com.cinemx.movies.feature.movies.domain.MovieRepository
import com.cinemx.movies.navigation.MovieDetailRoute
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<MovieRepository>()
    private val useCase = GetMovieDetailUseCase(repository)

    @Test
    fun `emite Loading y luego Success cuando el repositorio responde`() = runTest {
        coEvery { repository.getMovieDetail(MOVIE_ID) } returns Result.success(movieDetail)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(MovieDetailUiState.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is MovieDetailUiState.Success)
            assertEquals(movieDetail, (success as MovieDetailUiState.Success).movie)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emite Error cuando el repositorio falla`() = runTest {
        coEvery { repository.getMovieDetail(MOVIE_ID) } returns
            Result.failure(NetworkException(NetworkError.NoConnection))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(MovieDetailUiState.Loading, awaitItem())
            assertTrue(awaitItem() is MovieDetailUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry vuelve a consultar y recupera el detalle tras un fallo`() = runTest {
        coEvery { repository.getMovieDetail(MOVIE_ID) } returnsMany listOf(
            Result.failure(NetworkException(NetworkError.Server)),
            Result.success(movieDetail),
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(MovieDetailUiState.Loading, awaitItem())
            assertTrue(awaitItem() is MovieDetailUiState.Error)

            viewModel.retry()

            assertEquals(MovieDetailUiState.Loading, awaitItem())
            assertTrue(awaitItem() is MovieDetailUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel() = MovieDetailViewModel(
        getMovieDetail = useCase,
        // Misma clave que Navigation usa para el argumento de la ruta de detalle.
        savedStateHandle = SavedStateHandle(mapOf(MovieDetailRoute.MOVIE_ID_ARG to MOVIE_ID)),
    )

    private companion object {
        const val MOVIE_ID = 1234

        val movieDetail = MovieDetail(
            id = MOVIE_ID,
            title = "Una película",
            posterUrl = null,
            backdropUrl = null,
            overview = "Sinopsis",
            runtime = "1 h 40 min",
            releaseDate = "12 sept 2026",
            certification = "B",
            rating = "7.5",
            genres = listOf("Drama"),
        )
    }
}
