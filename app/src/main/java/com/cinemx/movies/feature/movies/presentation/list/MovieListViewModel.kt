package com.cinemx.movies.feature.movies.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cinemx.movies.feature.auth.domain.AuthRepository
import com.cinemx.movies.feature.auth.domain.LogoutUseCase
import com.cinemx.movies.feature.auth.domain.SessionState
import com.cinemx.movies.feature.movies.domain.GetNowPlayingMoviesUseCase
import com.cinemx.movies.feature.movies.domain.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MovieListEvent {
    data object NavigateToLogin : MovieListEvent
}

@HiltViewModel
class MovieListViewModel @Inject constructor(
    getNowPlayingMovies: GetNowPlayingMoviesUseCase,
    authRepository: AuthRepository,
    private val logout: LogoutUseCase,
) : ViewModel() {

    /** Sin `cachedIn`, cada recreación de la Activity volvería a pedir la página 1. */
    val movies: Flow<PagingData<Movie>> = getNowPlayingMovies().cachedIn(viewModelScope)

    val userName: StateFlow<String> = authRepository.sessionState
        .map { state -> (state as? SessionState.Authenticated)?.user?.greetingName.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = "",
        )

    private val _events = Channel<MovieListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
