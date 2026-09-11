package com.cinemx.movies.feature.movies.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.cinemx.movies.R
import com.cinemx.movies.core.network.NetworkException
import com.cinemx.movies.core.network.toNetworkError
import com.cinemx.movies.core.network.toUiText
import com.cinemx.movies.core.ui.components.BrandHeaderScaffold
import com.cinemx.movies.core.ui.components.BrandMark
import com.cinemx.movies.core.ui.components.EmptyView
import com.cinemx.movies.core.ui.components.ErrorView
import com.cinemx.movies.core.ui.components.LoadingView
import com.cinemx.movies.core.ui.theme.onBrandVariant
import com.cinemx.movies.feature.movies.domain.Movie
import kotlinx.coroutines.flow.collectLatest

private val HEADER_MARK_SIZE = 44.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    onMovieClick: (Int) -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieListViewModel = hiltViewModel(),
) {
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val selectedGenreId by viewModel.selectedGenreId.collectAsStateWithLifecycle()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                MovieListEvent.NavigateToLogin -> onLoggedOut()
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.onLogoutConfirmed()
            },
            onDismiss = { showLogoutDialog = false },
        )
    }

    BrandHeaderScaffold(
        modifier = modifier,
        header = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandMark(size = HEADER_MARK_SIZE, elevation = 6.dp)

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_greeting, userName),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        // `greetingName` puede ser el nombre completo del proveedor:
                        // con el logotipo al lado, uno largo desbordaría la cabecera.
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = onBrandVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(onClick = { showLogoutDialog = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = stringResource(R.string.home_logout),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            GenreFilterRow(
                genres = genres,
                selectedGenreId = selectedGenreId,
                onGenreSelected = viewModel::onGenreSelected,
                modifier = Modifier.padding(top = 18.dp),
            )
        },
    ) {
        MovieListContent(
            movies = movies,
            isFiltered = selectedGenreId != null,
            onMovieClick = onMovieClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieListContent(
    movies: LazyPagingItems<Movie>,
    isFiltered: Boolean,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = movies.loadState.refresh
    // Solo con contenido ya en pantalla; la primera carga ocupa toda la vista.
    val isRefreshing = refreshState is LoadState.Loading && movies.itemCount > 0

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = movies::refresh,
        modifier = modifier,
    ) {
        when {
            refreshState is LoadState.Loading && movies.itemCount == 0 -> LoadingView()

            refreshState is LoadState.Error && movies.itemCount == 0 -> ErrorView(
                message = refreshState.error.toMessage(),
                onRetry = movies::retry,
            )

            refreshState is LoadState.NotLoading && movies.itemCount == 0 -> EmptyView(
                title = stringResource(
                    if (isFiltered) R.string.home_empty_genre_title else R.string.home_empty_title,
                ),
                message = stringResource(
                    if (isFiltered) {
                        R.string.home_empty_genre_message
                    } else {
                        R.string.home_empty_message
                    },
                ),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(
                    count = movies.itemCount,
                    // Clave estable: evita reutilizar la tarjeta equivocada al llegar una página.
                    key = movies.itemKey { it.id },
                ) { index ->
                    movies[index]?.let { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                        )
                    }
                }

                when (val append = movies.loadState.append) {
                    is LoadState.Loading -> item { AppendLoading() }
                    is LoadState.Error -> item {
                        AppendError(error = append.error, onRetry = movies::retry)
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun AppendLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun AppendError(error: Throwable, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error.toMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.common_retry))
            }
        }
    }
}

@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_logout_title)) },
        text = { Text(stringResource(R.string.home_logout_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.home_logout_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_logout_cancel))
            }
        },
    )
}

@Composable
private fun Throwable.toMessage(): String {
    val error = (this as? NetworkException)?.error ?: toNetworkError()
    return error.toUiText().asString(LocalContext.current)
}
