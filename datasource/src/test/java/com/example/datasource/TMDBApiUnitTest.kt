package com.example.datasource

import com.example.datasource.remote.ApiClient
import com.example.datasource.remote.api.TMDBApiServiceV3
import com.example.datasource.remote.api.TMDBApiServiceV4
import com.example.datasource.remote.models.responses.*
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TMDBApiUnitTest {

    private val apiV4 = ApiClient().retrofit.create(TMDBApiServiceV4::class.java)
    private val apiV3 = ApiClient().retrofit.create(TMDBApiServiceV3::class.java)

    @Test
    fun `POST-Request Token`() = runBlocking {
        val response: Response<RequestTokenResponse> =
            apiV4.requestToken("android://com.example.movieland/ui/auth/AuthActivity")
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Now Playing Movies`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchNowPlayingMovies()
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Popular Movies`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchPopularMovies()
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Popular TV Shows`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchPopularTvShows()
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Top Rated Movies`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchTopRatedMovies()
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Upcoming Movies`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchUpcomingMovies()
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Trending Movies`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchTrending(
            mediaType = "movie",
            timeWindow = "week"
        )
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Trending TV Shows`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchTrending(
            mediaType = "tv",
            timeWindow = "week"
        )
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Movie Detail`() = runBlocking {
        val response: Response<MovieDetailResponse> = apiV3.fetchMovieDetail(movieId = 451048)
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Tv Show Detail`() = runBlocking {
        val response: Response<TvShowDetailsResponse> =
            apiV3.fetchTvShowDetail(tvId = 91363)  // what if show id
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Searched Movies`() = runBlocking {
        val response: Response<MovieListResponse> =
            apiV3.fetchSearchQueryResults(searchQuery = "Avengers")
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Similar Movies`() = runBlocking {
        val response: Response<MovieListResponse> =
            apiV3.fetchSimilarMovies(movieId = 566525)
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Similar TV Shows`() = runBlocking {
        val response: Response<MovieListResponse> =
            apiV3.fetchSimilarShows(tvId = 91363)
        assertNotNull(response.body())
    }

    @Test
    fun `GET-TV Season Details`() = runBlocking {
        val response: Response<TvSeasonDetailResponse> =
            apiV3.fetchTvSeasonDetails(91363, 1)
        assertNotNull(response.body())
    }

    @Test
    fun `GET-TV Episode Details`() = runBlocking {
        val response: Response<TvEpisodeDetailResponse> =
            apiV3.fetchTvEpisodeDetails(91363, 1, 1) // WhatIf tv show id
        assertNotNull(response.body())
    }

}