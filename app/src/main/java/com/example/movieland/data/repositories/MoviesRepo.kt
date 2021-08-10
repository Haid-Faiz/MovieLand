package com.example.movieland.data.repositories

import com.example.datasource.remote.ApiClient
import com.example.datasource.remote.api.TMDBApiServiceV3

class MoviesRepo : BaseRepo() {

    private val apiV3 = ApiClient().buildApi(TMDBApiServiceV3::class.java)

    suspend fun fetchNowPlayingMovies() = safeApiCall {
        apiV3.fetchNowPlayingMovies()
    }

    suspend fun fetchTopRatedMovies() = safeApiCall {
        apiV3.fetchTopRatedMovies()
    }

    suspend fun fetchUpcomingMovies() = safeApiCall {
        apiV3.fetchUpcomingMovies()
    }

    suspend fun fetchPopularMovies() = safeApiCall {
        apiV3.fetchPopularMovies()
    }

    suspend fun fetchPopularTvShows() = safeApiCall {
        apiV3.fetchPopularTvShows()
    }
}