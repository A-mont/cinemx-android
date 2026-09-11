package com.cinemx.movies.feature.movies.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.cinemx.movies.core.network.NetworkError
import com.cinemx.movies.core.network.NetworkException
import com.cinemx.movies.feature.movies.data.remote.TmdbApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/** Contra un JSON real de TMDB guardado en `resources/`, sin depender de la red. */
class MovieRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)

        repository = MovieRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `mapea el detalle completo desde la respuesta real de TMDB`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(readJson("movie_detail.json")))

        val detail = repository.getMovieDetail(1064213).getOrThrow()

        assertEquals("Anora", detail.title)
        assertEquals("2 h 19 min", detail.runtime)
        assertEquals("7.1", detail.rating)
        assertEquals(listOf("Comedia", "Drama"), detail.genres)
    }

    @Test
    fun `toma la clasificacion de MX ignorando la entrada vacia`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(readJson("movie_detail.json")))

        val detail = repository.getMovieDetail(1064213).getOrThrow()

        assertEquals("C", detail.certification)
    }

    @Test
    fun `pide el detalle con append_to_response para resolverlo en una sola llamada`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(readJson("movie_detail.json")))

        repository.getMovieDetail(1064213)

        val path = server.takeRequest().path.orEmpty()
        assertTrue(path.startsWith("/movie/1064213"))
        assertTrue(path.contains("append_to_response=release_dates"))
    }

    @Test
    fun `traduce un 404 a NotFound`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("{}"))

        val error = repository.getMovieDetail(1).exceptionOrNull()

        assertTrue(error is NetworkException)
        assertEquals(NetworkError.NotFound, (error as NetworkException).error)
    }

    @Test
    fun `traduce un 500 a Server`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        val error = repository.getMovieDetail(1).exceptionOrNull()

        assertEquals(NetworkError.Server, (error as NetworkException).error)
    }

    @Test
    fun `traduce un 401 a Unauthorized`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))

        val error = repository.getMovieDetail(1).exceptionOrNull()

        assertEquals(NetworkError.Unauthorized, (error as NetworkException).error)
    }

    @Test
    fun `mapea el catalogo de generos descartando los vacios`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(GENRES_JSON))

        val genres = repository.getGenres().getOrThrow()

        assertEquals(listOf("Acción", "Terror"), genres.map { it.name })
        assertEquals(listOf(28, 27), genres.map { it.id })
    }

    @Test
    fun `no vuelve a pedir el catalogo de generos una vez resuelto`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(GENRES_JSON))

        repository.getGenres().getOrThrow()
        repository.getGenres().getOrThrow()

        assertEquals(1, server.requestCount)
    }

    @Test
    fun `un fallo del catalogo no se cachea`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
        server.enqueue(MockResponse().setResponseCode(200).setBody(GENRES_JSON))

        assertTrue(repository.getGenres().isFailure)

        assertEquals(2, repository.getGenres().getOrThrow().size)
    }

    private fun readJson(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "Falta $name en resources" }
            .bufferedReader()
            .use { it.readText() }

    private companion object {
        val GENRES_JSON = """
            {"genres":[
              {"id":28,"name":"Acción"},
              {"id":27,"name":"Terror"},
              {"id":99,"name":""}
            ]}
        """.trimIndent()
    }
}
