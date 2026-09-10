package com.cinemx.movies.feature.movies.data.mapper

import com.cinemx.movies.feature.movies.data.remote.CountryReleaseDatesDto
import com.cinemx.movies.feature.movies.data.remote.ReleaseDateDto
import com.cinemx.movies.feature.movies.data.remote.ReleaseDatesDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CertificationMapperTest {

    @Test
    fun `prefiere la clasificacion de MX cuando existe`() {
        val dto = releaseDates(
            "US" to listOf("PG-13"),
            "MX" to listOf("B"),
        )

        assertEquals("B", dto.certificationFor("MX", "US"))
    }

    @Test
    fun `usa US cuando MX no tiene clasificacion`() {
        val dto = releaseDates(
            "MX" to listOf(""),
            "US" to listOf("PG-13"),
        )

        assertEquals("PG-13", dto.certificationFor("MX", "US"))
    }

    @Test
    fun `toma la primera entrada no vacia dentro del mismo pais`() {
        val dto = releaseDates("MX" to listOf("", "  ", "C"))

        assertEquals("C", dto.certificationFor("MX", "US"))
    }

    @Test
    fun `devuelve null cuando ningun pais aporta clasificacion`() {
        val dto = releaseDates(
            "MX" to listOf(""),
            "US" to listOf(""),
        )

        assertNull(dto.certificationFor("MX", "US"))
    }

    @Test
    fun `devuelve null cuando el bloque release_dates no vino en la respuesta`() {
        assertNull(null.certificationFor("MX", "US"))
    }

    @Test
    fun `devuelve null cuando el pais solicitado no esta en la lista`() {
        val dto = releaseDates("FR" to listOf("12"))

        assertNull(dto.certificationFor("MX", "US"))
    }

    @Test
    fun `compara el codigo de pais sin distinguir mayusculas`() {
        val dto = releaseDates("mx" to listOf("A"))

        assertEquals("A", dto.certificationFor("MX"))
    }

    private fun releaseDates(vararg entries: Pair<String, List<String>>) = ReleaseDatesDto(
        results = entries.map { (country, certifications) ->
            CountryReleaseDatesDto(
                iso31661 = country,
                releaseDates = certifications.map { ReleaseDateDto(certification = it) },
            )
        },
    )
}
