package com.cinemx.movies.feature.movies.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.cinemx.movies.R
import com.cinemx.movies.core.ui.components.RatingBadge
import com.cinemx.movies.feature.movies.domain.Movie

private const val POSTER_ASPECT_RATIO = 2f / 3f
private val POSTER_WIDTH = 96.dp
private val CARD_SHAPE = RoundedCornerShape(20.dp)

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tarjeta plana: se separa del fondo por contraste de superficie, no por sombra.
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Poster(
                url = movie.posterUrl,
                title = movie.title,
                modifier = Modifier
                    .width(POSTER_WIDTH)
                    .aspectRatio(POSTER_ASPECT_RATIO)
                    .clip(RoundedCornerShape(14.dp)),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                movie.releaseDate?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                RatingBadge(rating = movie.rating ?: stringResource(R.string.common_not_available))
            }
        }
    }
}

/** TMDB devuelve `poster_path` nulo en algunos estrenos: sin respaldo quedaría un hueco. */
@Composable
private fun Poster(
    url: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = stringResource(R.string.detail_poster_of, title),
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { PosterPlaceholder() },
        error = { PosterPlaceholder() },
    )
}

@Composable
private fun PosterPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Movie,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
