package com.cinemx.movies.feature.movies.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.cinemx.movies.R
import com.cinemx.movies.core.ui.components.ErrorView
import com.cinemx.movies.core.ui.components.LoadingView
import com.cinemx.movies.core.ui.components.RatingBadge
import com.cinemx.movies.core.ui.components.SheetCornerRadius
import com.cinemx.movies.feature.movies.domain.MovieDetail

private const val BACKDROP_ASPECT_RATIO = 16f / 9f
private val CARD_SHAPE = RoundedCornerShape(18.dp)
private val Scrim = Color(0x66000000)

@Composable
fun MovieDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when (val current = state) {
            MovieDetailUiState.Loading -> LoadingView()

            is MovieDetailUiState.Error -> ErrorView(
                message = current.message.asString(),
                onRetry = viewModel::retry,
            )

            is MovieDetailUiState.Success -> DetailContent(movie = current.movie)
        }

        // Flota sobre el backdrop: ninguna barra superior compite con la imagen.
        BackButton(
            onBack = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp),
        )
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onBack,
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(50))
            .background(Scrim),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.detail_back),
            tint = Color.White,
        )
    }
}

@Composable
private fun DetailContent(movie: MovieDetail, modifier: Modifier = Modifier) {
    val notAvailable = stringResource(R.string.common_not_available)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Backdrop(url = movie.backdropUrl, title = movie.title)

        Surface(
            // Sube sobre la imagen, igual que la hoja de la pantalla principal.
            modifier = Modifier.offset(y = -SheetCornerRadius),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = SheetCornerRadius, topEnd = SheetCornerRadius),
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = movie.title, style = MaterialTheme.typography.headlineMedium)
                    RatingBadge(rating = movie.rating ?: notAvailable)
                }

                MetadataRow(movie = movie, notAvailable = notAvailable)

                if (movie.genres.isNotEmpty()) {
                    Section(title = stringResource(R.string.detail_genres)) {
                        GenreChips(genres = movie.genres)
                    }
                }

                Section(title = stringResource(R.string.detail_overview)) {
                    Text(
                        // TMDB devuelve sinopsis vacías en es-MX con cierta frecuencia.
                        text = movie.overview.ifBlank { stringResource(R.string.detail_no_overview) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Backdrop(url: String?, title: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = stringResource(R.string.detail_poster_of, title),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(BACKDROP_ASPECT_RATIO),
        loading = { BackdropPlaceholder() },
        error = { BackdropPlaceholder() },
    )
}

@Composable
private fun BackdropPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Movie,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/** Duración, estreno y clasificación como tres tarjetas planas del mismo peso. */
@Composable
private fun MetadataRow(movie: MovieDetail, notAvailable: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetadataCard(
            label = stringResource(R.string.detail_runtime),
            value = movie.runtime ?: notAvailable,
            modifier = Modifier.weight(1f),
        )
        MetadataCard(
            label = stringResource(R.string.detail_release_date),
            value = movie.releaseDate ?: notAvailable,
            modifier = Modifier.weight(1f),
        )
        MetadataCard(
            label = stringResource(R.string.detail_certification),
            value = movie.certification ?: notAvailable,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetadataCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(CARD_SHAPE)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreChips(genres: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        genres.forEach { genre ->
            Text(
                text = genre,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        content()
    }
}
