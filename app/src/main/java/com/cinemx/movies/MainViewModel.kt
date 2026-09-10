package com.cinemx.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemx.movies.feature.auth.domain.AuthRepository
import com.cinemx.movies.feature.auth.domain.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Mientras el estado sea [SessionState.Resolving], el splash sigue en pantalla. */
@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = authRepository.sessionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Resolving,
        )
}
