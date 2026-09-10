package com.cinemx.movies.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val SPANISH_MX = Locale.forLanguageTag("es-MX")

private val MEDIUM_DATE = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.MEDIUM)
    .withLocale(SPANISH_MX)

/** Devuelve `null` con `0` o `null`, para que la UI decida el texto de respaldo. */
fun Int?.toRuntimeText(): String? = this
    ?.takeIf { it > 0 }
    ?.let { minutes ->
        val hours = minutes / MINUTES_PER_HOUR
        val rest = minutes % MINUTES_PER_HOUR
        when {
            hours == 0 -> "$rest min"
            rest == 0 -> "$hours h"
            else -> "$hours h $rest min"
        }
    }

/** TMDB manda cadena vacía cuando la fecha no está confirmada, de ahí el `runCatching`. */
fun String?.toReleaseDateText(): String? = this
    ?.takeIf { it.isNotBlank() }
    ?.let { raw -> runCatching { LocalDate.parse(raw).format(MEDIUM_DATE) }.getOrNull() }

/** Locale fijo: el separador decimal no debe depender del dispositivo. */
fun Double?.toRatingText(): String? = this
    ?.takeIf { it > 0.0 }
    ?.let { String.format(SPANISH_MX, "%.1f", it) }

private const val MINUTES_PER_HOUR = 60
