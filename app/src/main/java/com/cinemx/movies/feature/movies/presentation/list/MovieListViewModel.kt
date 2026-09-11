package com.cinemx.movies.feature.movies.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cinemx.movies.feature.auth.domain.AuthRepository
import com.cinemx.movies.feature.auth.domain.LogoutUseCase
import com.cinemx.movies.feature.auth.domain.SessionState
import com.cinemx.movies.feature.movies.domain.Genre
import com.cinemx.movies.feature.movies.domain.GetMovieGenresUseCase
import com.cinemx.movies.feature.movies.domain.GetNowPlayingMoviesUseCase
import com.cinemx.movies.feature.movies.domain.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MovieListEvent {
    data object NavigateToLogin : MovieListEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovieListViewModel @Inject constructor(
    getNowPlayingMovies: GetNowPlayingMoviesUseCase,
    private val getMovieGenres: GetMovieGenresUseCase,
    authRepository: AuthRepository,
    private val logout: LogoutUseCase,
) : ViewModel() {

    /** `null` es «todos los géneros», el estado por defecto. */
    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    /** Sin `cachedIn`, cada recreación de la Activity volvería a pedir la página 1. */
    val movies: Flow<PagingData<Movie>> = _selectedGenreId
        .flatMapLatest { genreId -> getNowPlayingMovies(genreId) }
        .cachedIn(viewModelScope)

    val userName: StateFlow<String> = authRepository.sessionState
        .map { state -> (state as? SessionState.Authenticated)?.user?.greetingName.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = "",
        )

    private val _events = Channel<MovieListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Si falla, la fila de filtros no se dibuja y el listado sigue funcionando.
        viewModelScope.launch {
            getMovieGenres().onSuccess { _genres.value = it }
        }
    }

    fun onGenreSelected(genreId: Int?) {
        _selectedGenreId.value = genreId
    }

    fun onLogoutConfirmed() {
        viewModelScope.launch {
            // Se navega aunque falle: dejar al usuario dentro sería peor que sacarlo.
            logout()
            _events.send(MovieListEvent.NavigateToLogin)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
