package com.cinemx.movies.feature.movies.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cinemx.movies.R
import com.cinemx.movies.core.ui.theme.brandChipGradient
import com.cinemx.movies.core.ui.theme.onBrandChip
import com.cinemx.movies.feature.movies.domain.Genre

private const val ALL_GENRES_KEY = "all-genres"

private val GEM_SHAPE = RoundedCornerShape(percent = 50)
private const val PRESSED_SCALE = 0.94f
private const val SELECTION_TRANSITION_MS = 220

/** Cara de vidrio: el borde superior recoge más luz que el inferior. */
private val GLASS_FILL = Brush.verticalGradient(
    listOf(Color.White.copy(alpha = 0.26f), Color.White.copy(alpha = 0.10f)),
)
private val GLASS_EDGE = Brush.verticalGradient(
    listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.18f)),
)

@Composable
fun GenreFilterRow(
    genres: List<Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Sin catálogo no hay nada que filtrar: la fila desaparece y el listado sigue igual.
    if (genres.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = ALL_GENRES_KEY) {
            GemChip(
                label = stringResource(R.string.home_genre_all),
                selected = selectedGenreId == null,
                onClick = { onGenreSelected(null) },
            )
        }

        items(items = genres, key = { it.id }) { genre ->
            GemChip(
                label = genre.name,
                selected = genre.id == selectedGenreId,
                onClick = { onGenreSelected(genre.id) },
            )
        }
    }
}

@Composable
private fun GemChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = animateFloatAsState(
        targetValue = if (isPressed) PRESSED_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "gemScale",
    )
    // Cruza el vidrio con la gema en lugar de cambiar el relleno de golpe.
    val selection = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = SELECTION_TRANSITION_MS),
        label = "gemSelection",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) onBrandChip else Color.White,
        animationSpec = tween(durationMillis = SELECTION_TRANSITION_MS),
        label = "gemContent",
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(GEM_SHAPE)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(GLASS_FILL)
                .border(1.dp, GLASS_EDGE, GEM_SHAPE),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = selection.value }
                .background(brandChipGradient)
                .border(1.dp, Color.White, GEM_SHAPE),
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}
