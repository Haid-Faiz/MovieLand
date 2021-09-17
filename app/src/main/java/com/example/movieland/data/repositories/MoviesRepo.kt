package com.example.movieland.data.repositories

import com.example.datasource.remote.api.TMDBApiServiceV3
import com.example.movieland.utils.SessionPrefs
import retrofit2.http.Path
import javax.inject.Inject

class MoviesRepo @Inject constructor(
    private val apiV3: TMDBApiServiceV3,
    private val sessionPrefs: SessionPrefs
) : BaseRepo(sessionPrefs) {

//    private val apiV3 = ApiClient().buildApi(TMDBApiServiceV3::class.java)

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

    suspend fun fetchTrendingMovies() = safeApiCall {
        apiV3.fetchTrending(mediaType = "movie", timeWindow = "week")
    }

    suspend fun fetchTrendingTvShows() = safeApiCall {
        apiV3.fetchTrending(mediaType = "tv", timeWindow = "week")
    }

    suspend fun fetchSearchedMovies(searchQuery: String) = safeApiCall {
        apiV3.fetchSearchQueryResults(searchQuery = searchQuery)
    }

    suspend fun fetchMovieDetail(movieId: Int) = safeApiCall {
        apiV3.fetchMovieDetail(movieId = movieId)
    }

    suspend fun fetchTvShowDetail(tvId: Int) = safeApiCall {
        apiV3.fetchTvShowDetail(tvId = tvId)
    }

    suspend fun fetchSimilarMovies(movieId: Int) = safeApiCall {
        apiV3.fetchSimilarMovies(movieId = movieId)
    }

    suspend fun fetchSimilarShows(tvID: Int) = safeApiCall {
        apiV3.fetchSimilarShows(tvId = tvID)
    }

    suspend fun fetchTvSeasonDetails(tvId: Int, seasonNumber: Int) = safeApiCall {
        apiV3.fetchTvSeasonDetails(tvId = tvId, seasonNumber)
    }

    suspend fun fetchTvEpisodeDetails(
        tvId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ) = safeApiCall {
        apiV3.fetchTvEpisodeDetails(tvId = tvId, seasonNumber, episodeNumber)
    }

}