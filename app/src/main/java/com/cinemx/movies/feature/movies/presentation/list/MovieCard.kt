package com.cinemx.movies.feature.movies.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
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
private val POSTER_SHAPE = RoundedCornerShape(14.dp)
private val CARD_SHAPE = RoundedCornerShape(20.dp)
private val CARD_BORDER_WIDTH = 1.5.dp
private const val PRESSED_SCALE = 0.975f
private const val BORDER_ALPHA_IDLE = 0.35f
private const val BORDER_ALPHA_PRESSED = 1f
private const val BORDER_TRANSITION_MS = 180

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) PRESSED_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cardScale",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isPressed) BORDER_ALPHA_PRESSED else BORDER_ALPHA_IDLE,
        animationSpec = tween(durationMillis = BORDER_TRANSITION_MS),
        label = "cardBorderAlpha",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = tween(durationMillis = BORDER_TRANSITION_MS),
        label = "cardContainer",
    )

    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outlineVariant
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            primary.copy(alpha = borderAlpha),
            outline.copy(alpha = borderAlpha),
            primary.copy(alpha = borderAlpha * 0.5f),
        ),
    )

    // Tarjeta plana: se separa del fondo por contraste de superficie, no por sombra.
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(CARD_BORDER_WIDTH, borderBrush),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Poster(
                url = movie.posterUrl,
                title = movie.title,
                modifier = Modifier
                    .width(POSTER_WIDTH)
                    .aspectRatio(POSTER_ASPECT_RATIO)
                    .clip(POSTER_SHAPE)
                    .border(1.dp, outline.copy(alpha = 0.6f), POSTER_SHAPE),
            )

            Column(
                modifier = Modifier.weight(1f),
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

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
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
