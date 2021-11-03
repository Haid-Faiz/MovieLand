package com.example.datasource.remote

import com.example.datasource.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {

    private val TMDB_BASE_URL = "https://api.themoviedb.org/"

    private val okHttpBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient.Builder()
            .callTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(TMDB_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(
                okHttpBuilder.addInterceptor(
                    Interceptor { chain: Interceptor.Chain ->
                        val request = chain.request()
                            .newBuilder()
                            .header("Authorization", "Bearer ${BuildConfig.READ_ACCESS_TOKEN}")
                            .build()
                        chain.proceed(request)
                    }
                ).build()
            ).build()
    }

    // fun <T> buildApi(api: Class<T>): T = retrofit.create(api)
}
