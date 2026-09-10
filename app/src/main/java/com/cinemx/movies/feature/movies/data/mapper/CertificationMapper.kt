package com.cinemx.movies.feature.movies.data.mapper

import com.cinemx.movies.feature.movies.data.remote.ReleaseDatesDto

/**
 * TMDB separa la clasificación por país y muchas entradas llegan vacías: se recorren los
 * países en el orden recibido y, dentro de cada uno, se toma la primera no vacía.
 */
fun ReleaseDatesDto?.certificationFor(vararg countries: String): String? {
    val dto = this ?: return null
    return countries.firstNotNullOfOrNull { code ->
        dto.results
            .firstOrNull { it.iso31661.equals(code, ignoreCase = true) }
            ?.releaseDates
            ?.map { it.certification.trim() }
            ?.firstOrNull { it.isNotBlank() }
    }
}
