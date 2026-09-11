package com.cinemx.movies.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cinemx.movies.core.ui.theme.brandGradient

/** Aire de color entre el final de la cabecera y el borde redondeado de la hoja. */
private val HEADER_BOTTOM_SPACE = 24.dp

val SheetCornerRadius = 28.dp

/**
 * Cabecera con el degradado de marca y, debajo, la hoja de contenido con las esquinas
 * superiores redondeadas recortadas sobre ese color. Es el patrón que comparten login,
 * listado y detalle, así que el degradado se declara una sola vez.
 *
 * El color se dibuja por debajo de la status bar (la Activity ya es edge-to-edge) y es
 * [header] quien recibe el inset, para que su contenido no quede tapado.
 */
@Composable
fun BrandHeaderScaffold(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brandGradient)
                .statusBarsPadding()
                .padding(bottom = HEADER_BOTTOM_SPACE),
            content = header,
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = SheetCornerRadius, topEnd = SheetCornerRadius),
            content = content,
        )
    }
}
