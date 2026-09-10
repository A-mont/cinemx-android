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
    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") append: String = APPEND_RELEASE_DATES,
    ): MovieDetailDto

    companion object {
        const val DEFAULT_REGION = "MX"
        const val APPEND_RELEASE_DATES = "release_dates"
    }
}
