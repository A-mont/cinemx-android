package com.cinemx.movies.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FormattersTest {

    @Test
    fun `formatea horas y minutos`() {
        assertEquals("2 h 15 min", 135.toRuntimeText())
    }

    @Test
    fun `omite los minutos cuando la duracion es exacta en horas`() {
        assertEquals("2 h", 120.toRuntimeText())
    }

    @Test
    fun `omite las horas cuando dura menos de una`() {
        assertEquals("45 min", 45.toRuntimeText())
    }

    @Test
    fun `devuelve null cuando TMDB manda cero o null`() {
        assertNull(0.toRuntimeText())
        assertNull((null as Int?).toRuntimeText())
    }

    @Test
    fun `formatea la fecha de estreno a texto localizado`() {
        val formatted = "2026-09-12".toReleaseDateText()

        assertNotNull(formatted)
        // El patrón exacto depende de la JVM; lo estable es que incluya día y año.
        assertEquals(true, formatted!!.contains("12"))
        assertEquals(true, formatted.contains("2026"))
    }

    @Test
    fun `devuelve null cuando la fecha viene vacia o mal formada`() {
        assertNull("".toReleaseDateText())
        assertNull((null as String?).toReleaseDateText())
        assertNull("no-es-una-fecha".toReleaseDateText())
    }

    @Test
    fun `formatea la calificacion con un decimal`() {
        assertEquals("7.5", 7.456.toRatingText())
        assertEquals("8.0", 8.0.toRatingText())
    }

    @Test
    fun `devuelve null cuando la pelicula aun no tiene votos`() {
        assertNull(0.0.toRatingText())
        assertNull((null as Double?).toRatingText())
    }
}
