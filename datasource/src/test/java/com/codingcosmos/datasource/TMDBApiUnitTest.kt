package com.codingcosmos.datasource

import com.codingcosmos.datasource.remote.ApiClient
import com.codingcosmos.datasource.remote.api.TMDBApiServiceV3
import com.codingcosmos.datasource.remote.api.TMDBApiServiceV4
import com.codingcosmos.datasource.remote.models.responses.ActorFilmography
import com.codingcosmos.datasource.remote.models.responses.MovieDetailResponse
import com.codingcosmos.datasource.remote.models.responses.MovieListResponse
import com.codingcosmos.datasource.remote.models.responses.RequestTokenResponse
import com.codingcosmos.datasource.remote.models.responses.TvEpisodeDetailResponse
import com.codingcosmos.datasource.remote.models.responses.TvSeasonDetailResponse
import com.codingcosmos.datasource.remote.models.responses.TvShowDetailsResponse
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TMDBApiUnitTest {

    private val apiV4 = ApiClient().retrofit.create(TMDBApiServiceV4::class.java)
    private val apiV3 = ApiClient().retrofit.create(TMDBApiServiceV3::class.java)

    @Test
    fun `POST-Request Token`() = runBlocking {
        val response: Response<RequestTokenResponse> =
            apiV4.requestToken("android://com.codingcosmos.movieland/ui/auth/AuthActivity")
        assertNotNull(response.body())
    }

//    @Test
//    fun `POST-Create Session Id From V4 valid Access Token`() = runBlocking {
//        val response: Response<SessionResponse> =
//            apiV3.createSessionIdFromV4(v4AccessToken = "")
//        assertNotNull(response.body())
//    }

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
        val response: Response<MovieDetailResponse> =
            apiV3.fetchMovieDetail(movieId = 299534) // 451048 // DDLJ id: 19404
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Tv Show Detail`() = runBlocking {
        val response: Response<TvShowDetailsResponse> =
            apiV3.fetchTvShowDetail(tvId = 100128) // Lucifer id,, // what if show id:  91363, 81356, 63174
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Searched Movies`() = runBlocking {
        val response: Response<MovieListResponse> =
            apiV3.fetchMovieSearchedResults(searchQuery = "Avengers")
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Searched Shows`() = runBlocking {
        val response: Response<MovieListResponse> =
            apiV3.fetchTvSearchedResults(searchQuery = "Rick-Morty")
        assertNotNull(response.body())
    }

    @Test
    fun `GET-Similar Movies`() = runBlocking {
        val response: Response<MovieListResponse> =
            apiV3.fetchSimilarMovies(movieId = 566525)
        assertNotNull(response.body())
    }

    @Test
    fun `GET- Actor,Actress Filmography including movies & shows both`() = runBlocking {
        val response: Response<ActorFilmography> =
            apiV3.fetchActorFilmography(personId = 8784) // James Bond id: 8784
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
            apiV3.fetchTvSeasonDetails(
                63174,
                1
            ) // 100128, 133006 60735 we are getting 404 for this id (not found error)
        assertNotNull(response.body())
    }

    @Test
    fun `GET-TV Episode Details`() = runBlocking {
        val response: Response<TvEpisodeDetailResponse> =
            apiV3.fetchTvEpisodeDetails(91363, 1, 1) // WhatIf tv show id
        assertNotNull(response.body())
    }

//    @Test
//    fun `GET- User Account Details`() = runBlocking {
//        val response =
//            apiV3.getAccountDetails(sessionId = "")
//        assertNotNull(response.body())
//    }

//    @Test
//    fun `POST- Rate a Movie`() = runBlocking {
//        val response: Response<ResponseBody> = apiV3.rateMovie(
//            movieId = 566525,
//            sessionId = "",
//            mediaRatingRequest = MediaRatingRequest(6.1f)
//        )
//        assertNotNull(response.body())
//    }

//    @Test
//    fun `POST- Rate a TV Show`() = runBlocking {
//        val response = apiV3.rateTvShow(
//            tvId = 91363,
//            sessionId = "",
//            mediaRatingRequest = MediaRatingRequest(6.1f)
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `POST- Add Media to WatchList`() = runBlocking {
//        val response = apiV3.addToWatchList(
//            accountId = 454,
//            sessionId = "",
//            addToWatchListRequest = AddToWatchListRequest(
//                mediaId = 566525,
//                mediaType = "movie",
//                watchlist = true
//            )
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `POST- Add Media to Favourites`() = runBlocking {
//        val response = apiV3.addToFavourites(
//            accountId = 454,
//            sessionId = "",
//            addToFavouriteRequest = AddToFavouriteRequest(
//                mediaId = 566525,
//                mediaType = "movie",
//                favorite = true
//            )
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `Get- Favourites Movies List`() = runBlocking {
//        val response = apiV3.getFavouriteMovies(
//            accountId = 454,
//            sessionId = "",
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `Get- Favourites Tv Shows List`() = runBlocking {
//        val response = apiV3.getFavouriteTvShows(
//            accountId = 454,
//            sessionId = "",
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `Get- Rated Movies List`() = runBlocking {
//        val response = apiV3.getRatedMovies(
//            accountId = 454,
//            sessionId = "",
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `Get- Rated Tv Shows List`() = runBlocking {
//        val response = apiV3.getRatedTvShows(
//            accountId = 454,
//            sessionId = "da49333b7191f3f6d92b48d176943bb84ec19785",
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `Get- Movies WatchList`() = runBlocking {
//        val response = apiV3.getMoviesWatchList(
//            accountId = 454,
//            sessionId = "",
//        )
//        assertNotNull(response)
//    }

//    @Test
//    fun `Get- Tv Shows WatchList`() = runBlocking {
//        val response = apiV3.getTvShowsWatchList(
//            accountId = 454,
//            sessionId = "da49333b7191f3f6d92b48d176943bb84ec19785",
//        )
//        assertNotNull(response)
//    }
}
