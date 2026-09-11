package com.cinemx.movies.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cinemx.movies.R
import com.cinemx.movies.core.ui.theme.brandLogoGradient

private const val MARK_RATIO = 0.54f
private const val CORNER_RATIO = 0.29f
private const val BORDER_ALPHA = 0.35f

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    elevation: Dp = 8.dp,
    contentDescription: String? = null,
) {
    val shape = RoundedCornerShape(size * CORNER_RATIO)

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = elevation, shape = shape)
            .background(brandLogoGradient, shape)
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), shape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_app_logo),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * MARK_RATIO),
        )
    }
}
