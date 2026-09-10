package com.cinemx.movies.core.network

import com.cinemx.movies.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TmdbInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val url = original.url.newBuilder()
            .addQueryParameter("language", LANGUAGE)
            .build()

        val request = original.newBuilder()
            .url(url)
            .header("Authorization", "Bearer ${BuildConfig.TMDB_READ_TOKEN}")
            .header("Accept", "application/json")
            .build()

        return chain.proceed(request)
    }

    private companion object {
        const val LANGUAGE = "es-MX"
    }
}
