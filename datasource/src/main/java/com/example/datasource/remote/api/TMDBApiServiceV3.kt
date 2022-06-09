package com.example.datasource.remote.api

import com.example.datasource.remote.models.requests.AddToFavouriteRequest
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.requests.MediaRatingRequest
import com.example.datasource.remote.models.responses.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// TMDB API Version - 3
interface TMDBApiServiceV3 {

    @GET("3/movie/now_playing")
    suspend fun fetchNowPlayingMovies(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/movie/top_rated")
    suspend fun fetchTopRatedMovies(
        @Query("region") region: String = "IN",
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/movie/popular")
    suspend fun fetchPopularMovies(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/tv/popular")
    suspend fun fetchPopularTvShows(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/movie/upcoming")
    suspend fun fetchUpcomingMovies(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    // movie/451048?append_to_response=videos
    @GET("3/movie/{movie_id}")
    suspend fun fetchMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "videos",
        @Query("language") lang: String? = "en-US"
    ): Response<MovieDetailResponse>

    @GET("3/tv/{tv_id}")
    suspend fun fetchTvShowDetail(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") appendToResponse: String = "videos",
        @Query("language") lang: String? = "en-US"
    ): Response<TvShowDetailsResponse>

    @GET("3/trending/{media_type}/{time_window}")
    suspend fun fetchTrending(
        @Path("media_type") mediaType: String = "movie", // movie, tv, person, all
        @Path("time_window") timeWindow: String = "day", // day, week                       // day, week
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/movie/{movie_id}/similar")
    suspend fun fetchSimilarMovies(
        @Path("movie_id") movieId: Int,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/tv/{tv_id}/similar")
    suspend fun fetchSimilarShows(
        @Path("tv_id") tvId: Int,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/movie/{movie_id}/recommendations")
    suspend fun fetchRecommendedMovies(
        @Path("movie_id") movieId: Int,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/tv/{tv_id}/recommendations")
    suspend fun fetchRecommendedTvShow(
        @Path("tv_id") tvId: Int,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/search/movie")
    suspend fun fetchMovieSearchedResults(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("query") searchQuery: String
    ): Response<MovieListResponse>

    @GET("3/search/tv")
    suspend fun fetchTvSearchedResults(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("query") searchQuery: String
    ): Response<MovieListResponse>

    @GET("3/tv/{tv_id}/season/{season_number}")
    suspend fun fetchTvSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("append_to_response") appendToResponse: String = "videos",
        @Query("language") lang: String? = "en-US"
    ): Response<TvSeasonDetailResponse>

    @GET("3/tv/{tv_id}/season/{season_number}/episode/{episode_number}")
    suspend fun fetchTvEpisodeDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Path("episode_number") episodeNumber: Int,
        @Query("append_to_response") appendToResponse: String = "videos",
        @Query("language") lang: String? = "en-US"
    ): Response<TvEpisodeDetailResponse>

    @GET("3/discover/movie")
    suspend fun fetchMoviesByGenres(
        @Query("with_genres") genres: String, // It will require comma separated string of genres ids
        // @Query("include_video") includeVideo: Boolean = false,
        @Query("sort_by") sortBy: String? = "popularity.desc",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") lang: String? = "en-US"
    ): Response<MovieListResponse>

    @GET("3/discover/tv")
    suspend fun fetchTvShowsByGenres(
        @Query("with_genres") genres: String, // genres = "24,14,35...."
        @Query("sort_by") sortBy: String? = "popularity.desc",
        // @Query("include_video") includeVideo: Boolean = false,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") lang: String? = "en-US"
    ): Response<MovieListResponse>

    @GET("3/discover/tv")
    suspend fun fetchAnimeSeries(
        @Query("with_genres") genres: String = "16", // Animation genre = "16"
        @Query("sort_by") sortBy: String? = "popularity.desc",
        @Query("first_air_date.gte") firstAirDateGreaterThan: String = "2010-01-01",
        @Query("page") page: Int = 1,
        @Query("language") lang: String? = "en-US",
        @Query("with_original_language") origLang: String = "en",
        @Query("include_null_first_air_dates") include: Boolean = false
    ): Response<MovieListResponse>

    @GET("3/discover/movie")
    suspend fun fetchBollywoodMovies(
        @Query("sort_by") sortBy: String? = "popularity.desc",
        @Query("primary_release_date.gte") releaseDateGreaterThan: String = "2012-08-01",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "IN",
        @Query("with_release_type") releaseType: String = "3|2",
        @Query("watch_region") watchRegion: String = "IN",
        @Query("language") lang: String? = "hi-IN",
        @Query("with_original_language") origLang: String = "hi",
    ): Response<MovieListResponse>

    @GET("3/movie/{movie_id}/credits")
    suspend fun fetchMovieCast(
        @Path("movie_id") movieId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<MediaCastResponse>

    @GET("3/tv/{tv_id}/credits")
    suspend fun fetchTvShowsCast(
        @Path("tv_id") tvId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<MediaCastResponse>

    @GET("3/person/{person_id}/combined_credits")
    suspend fun fetchActorFilmography(
        @Path("person_id") personId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<ActorFilmography>

    // -------------------------------These Requests requires SESSION ID-----------------------------

    @GET("3/account")
    suspend fun getAccountDetails(
        @Query("session_id") sessionId: String
    ): Response<AccountDetailsResponse>

    @POST("3/movie/{movie_id}/rating")
    suspend fun rateMovie(
        @Path("movie_id") movieId: Int,
        @Query("session_id") sessionId: String,
        @Body mediaRatingRequest: MediaRatingRequest
    ): Response<ResponseBody>

    @POST("3/tv/{tv_id}/rating")
    suspend fun rateTvShow(
        @Path("tv_id") tvId: Int,
        @Query("session_id") sessionId: String,
        @Body mediaRatingRequest: MediaRatingRequest
    ): Response<ResponseBody>

    @POST("3/account/{account_id}/watchlist")
    suspend fun addToWatchList(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Body addToWatchListRequest: AddToWatchListRequest
    ): Response<ResponseBody>

    @POST("3/account/{account_id}/favorite")
    suspend fun addToFavourites(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Body addToFavouriteRequest: AddToFavouriteRequest
    ): Response<ResponseBody>

    @DELETE("3/movie/{movie_id}/rating")
    suspend fun deleteMovieRating(
        @Path("movie_id") movieId: Int,
        @Query("session_id") sessionId: String,
    ): Response<ResponseBody>

    @DELETE("3/tv/{tv_id}/rating")
    suspend fun deleteTvRating(
        @Path("tv_id") tvId: Int,
        @Query("session_id") sessionId: String,
    ): Response<ResponseBody>

    @POST("3/authentication/session/convert/4")
    @FormUrlEncoded
    suspend fun createSessionIdFromV4(
        @Field("access_token") v4AccessToken: String
    ): Response<SessionResponse>

    @GET("3/account/{account_id}/favorite/movies")
    suspend fun getFavouriteMovies(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc" // created_at.asc
    ): Response<MovieListResponse>

    @GET("3/account/{account_id}/favorite/tv")
    suspend fun getFavouriteTvShows(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc" // created_at.asc
    ): Response<MovieListResponse>

    @GET("3/account/{account_id}/rated/movies")
    suspend fun getRatedMovies(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc" // created_at.asc
    ): Response<MovieListResponse>

    @GET("3/account/{account_id}/rated/tv")
    suspend fun getRatedTvShows(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc" // created_at.asc
    ): Response<MovieListResponse>

    @GET("3/account/{account_id}/watchlist/movies")
    suspend fun getMoviesWatchList(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc" // created_at.asc
    ): Response<MovieListResponse>

    @GET("3/account/{account_id}/watchlist/tv")
    suspend fun getTvShowsWatchList(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.desc" // created_at.asc
    ): Response<MovieListResponse>

    @GET("3/tv/{tv_id}/external_ids")
    suspend fun getTvShowExternalIds(
        @Path("tv_id") tvId: Int,
    ): Response<TvShowExternalIdsResponse>

    @GET("3/movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movie_id") movieId: Int,
    ): Response<WatchProvidersResponse>

    @GET("3/tv/{tv_id}/watch/providers")
    suspend fun getTvWatchProviders(
        @Path("tv_id") tvId: Int,
    ): Response<WatchProvidersResponse>

//    @GET("3/movie/latest")
//    suspend fun fetchLatestMovies(
//        @Query("language") lang: String? = "en-US",
//        @Query("page") page: Int = 1
//    ) : Response<>
}

// @GET("movie/{id}?append_to_response=similar,videos")
// suspend fun fetchMovieDetails(@Path("id") movieId: Int): MovieDetailsResponse
//
// @GET("tv/{id}?append_to_response=similar,videos")
// suspend fun fetchTvDetails(@Path("id") tvId: Int): TvDetailsResponse
//
// ***************************************************************************8
// @GET("search/multi") // Multi endpoint searches Movies, Tv Shows, Persons
// suspend fun fetchSearchResults(
//    @Query("query") query: String,
//    @Query("page") page: Int
// ): MediaResponse
