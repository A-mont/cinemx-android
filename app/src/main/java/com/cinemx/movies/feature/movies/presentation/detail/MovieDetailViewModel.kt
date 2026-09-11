package com.cinemx.movies.feature.movies.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemx.movies.core.network.NetworkException
import com.cinemx.movies.core.network.toNetworkError
import com.cinemx.movies.core.network.toUiText
import com.cinemx.movies.core.util.UiText
import com.cinemx.movies.feature.movies.domain.GetMovieDetailUseCase
import com.cinemx.movies.feature.movies.domain.MovieDetail
import com.cinemx.movies.navigation.MovieDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MovieDetailUiState {
    data object Loading : MovieDetailUiState
    data class Success(val movie: MovieDetail) : MovieDetailUiState
    data class Error(val message: UiText) : MovieDetailUiState
}

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieDetail: GetMovieDetailUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle.get<Int>(MovieDetailRoute.MOVIE_ID_ARG)) {
        "Falta el argumento ${MovieDetailRoute.MOVIE_ID_ARG} en la ruta de detalle."
    }

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            _uiState.value = getMovieDetail(movieId).fold(
                onSuccess = { MovieDetailUiState.Success(it) },
                onFailure = { throwable ->
                    val error = (throwable as? NetworkException)?.error ?: throwable.toNetworkError()
                    MovieDetailUiState.Error(error.toUiText())
                },
            )
        }
    }
}
