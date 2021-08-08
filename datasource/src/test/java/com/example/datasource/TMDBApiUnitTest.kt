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

    private val apiV4 = ApiClient().buildApi(TMDBApiServiceV4::class.java)
    private val apiV3 = ApiClient().buildApi(TMDBApiServiceV3::class.java)

    @Test
    fun `POST-Request Token`() = runBlocking {
        val response: Response<RequestTokenResponse> = apiV4.requestToken("android://com.example.movieland/ui/auth/AuthActivity")
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Now Playing Movies`() = runBlocking {
        val response: Response<MovieListResponse> = apiV3.fetchNowPlayingMovies()
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

//    @Test
//    fun `GET-Trending Movies`() = runBlocking {
//        val response: Response<MovieListResponse> = apiV3.fetchTrending()
//        assertNotNull(response.body())
//    }
//
//    @Test
//    fun `GET-Trending Shows`() = runBlocking {
//        val response: Response<MovieListResponse> = apiV3.fetchTrending()
//        assertNotNull(response.body())
//    }

    @Test
    fun `GET-Movie Detail`() = runBlocking {
        val response: Response<MovieDetailResponse> = apiV3.fetchMovieDetail(movieId = "451048")
        assertNotNull(response.body())
    }

}