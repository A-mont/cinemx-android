package com.cinemx.movies.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = Indigo90,
    onPrimaryContainer = Indigo10,
    secondary = Indigo50,
    onSecondary = Color.White,
    secondaryContainer = Indigo90,
    onSecondaryContainer = Indigo10,
    tertiary = Navy,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceLight,
    surfaceContainerLow = SurfaceContainerLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighLight,
    outlineVariant = OutlineVariantLight,
    error = Crimson40,
    onError = Color.White,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = Crimson40,
)

private val DarkColors = darkColorScheme(
    primary = Indigo80,
    onPrimary = Indigo20,
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = Indigo90,
    secondary = Indigo80,
    onSecondary = Indigo20,
    secondaryContainer = IndigoContainerDark,
    onSecondaryContainer = Indigo90,
    tertiary = Indigo80,
    onTertiary = Indigo20,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = BackgroundDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = BackgroundDark,
    surfaceContainerLow = SurfaceContainerDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighDark,
    outlineVariant = OutlineVariantDark,
    error = Crimson80,
    onError = Color(0xFF5F0021),
    errorContainer = CrimsonContainerDark,
    onErrorContainer = Crimson80,
)

/**
 * Degradado de la cabecera de marca. Vive en el tema y no en las pantallas para que
 * el color siga teniendo una sola fuente de verdad.
 */
val brandGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = if (isSystemInDarkTheme()) {
            listOf(IndigoContainerDark, BackgroundDark)
        } else {
            listOf(Indigo60, Indigo40)
        },
    )

/** Color legible sobre [brandGradient], para textos secundarios de la cabecera. */
val onBrandVariant: Color = Indigo80

@Composable
fun CineMxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Sin dynamicColor a propósito: en Android 12+ sustituiría la paleta de marca
    // por la del fondo de pantalla y la identidad de CineMx se perdería.
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
