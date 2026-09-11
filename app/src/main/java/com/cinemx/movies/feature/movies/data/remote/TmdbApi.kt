package com.cinemx.movies.feature.movies.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("page") page: Int,
        @Query("region") region: String = DEFAULT_REGION,
    ): PagedResponseDto<MovieDto>

    /**
     * La clasificación por edad no viene en `/movie/{id}`: vive en `release_dates`.
     * Se pide con `append_to_response` para resolver el detalle en **una sola llamada**.
     */
    /**
     * `now_playing` no admite filtro de género, así que al elegir uno se pasa a
     * `discover` reproduciendo su ventana: estreno en cine dentro de los últimos
     * [NOW_PLAYING_WINDOW_DAYS] días, ordenado por popularidad.
     */
    @GET("discover/movie")
    suspend fun discoverByGenre(
        @Query("page") page: Int,
        @Query("with_genres") genreId: Int,
        @Query("release_date.gte") releasedFrom: String,
        @Query("release_date.lte") releasedTo: String,
        @Query("region") region: String = DEFAULT_REGION,
        @Query("sort_by") sortBy: String = SORT_BY_POPULARITY,
        @Query("with_release_type") releaseType: String = THEATRICAL_RELEASE_TYPES,
    ): PagedResponseDto<MovieDto>

    @GET("genre/movie/list")
    suspend fun getGenres(): GenresResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") append: String = APPEND_RELEASE_DATES,
    ): MovieDetailDto

    companion object {
        const val DEFAULT_REGION = "MX"
        const val APPEND_RELEASE_DATES = "release_dates"
        const val SORT_BY_POPULARITY = "popularity.desc"

        /** 2 = estreno limitado, 3 = estreno general. Deja fuera digital y TV. */
        const val THEATRICAL_RELEASE_TYPES = "2|3"
        const val NOW_PLAYING_WINDOW_DAYS = 45L
    }
}
