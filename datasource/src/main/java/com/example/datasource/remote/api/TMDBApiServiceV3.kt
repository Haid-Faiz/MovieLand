package com.example.datasource.remote.api

import com.example.datasource.remote.models.responses.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// TMDB API Version - 4
interface TMDBApiServiceV3 {

    @GET("3/movie/now_playing")
    suspend fun fetchNowPlayingMovies(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>

    @GET("3/movie/top_rated")
    suspend fun fetchTopRatedMovies(
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

    //movie/451048?append_to_response=videos
    @GET("3/movie/{movie_id}")
    suspend fun fetchMovieDetail(
        @Path("movie_id") movieId: String,
        @Query("append_to_response") appendToResponse: String = "videos",
        @Query("language") lang: String? = "en-US"
    ): Response<MovieDetailResponse>


    @GET("3/trending/{media_type}/{time_window}")
    suspend fun fetchTrending(
        @Path("media_type") mediaType: String = "movie",   // movie, tv, person, all
        @Path("time_window") timeWindow: String = "day",        // day, week                       // day, week
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieListResponse>


    //    @GET("3/movie/latest")
//    suspend fun fetchLatestMovies(
//        @Query("language") lang: String? = "en-US",
//        @Query("page") page: Int = 1
//    ) : Response<>
}


//@GET("movie/{id}?append_to_response=similar,videos")
//suspend fun fetchMovieDetails(@Path("id") movieId: Int): MovieDetailsResponse
//
//@GET("movie/{id}/similar")
//suspend fun fetchSimilarMovies(@Path("id") movieId: Int): PageResponse<Movie>
//
//@GET("movie/{id}/videos")
//suspend fun fetchMovieVideos(@Path("id") movieId: Int): VideosResponse
//
//@GET("tv/top_rated")
//suspend fun fetchTopRatedTvs(): PageResponse<TvShow>
//
//@GET("tv/{id}?append_to_response=similar,videos")
//suspend fun fetchTvDetails(@Path("id") tvId: Int): TvDetailsResponse
//
//@GET("tv/{id}/similar")
//suspend fun fetchSimilarTvs(@Path("id") tvId: Int): PageResponse<TvShow>
//
//@GET("tv/{id}/videos")
//suspend fun fetchTvVideos(@Path("id") tvId: Int): VideosResponse
//
//@GET("tv/{tv_id}/season/{season_number}")
//suspend fun fetchTvSeasonDetails(
//    @Path("tv_id") tvId: Int,
//    @Path("season_number") seasonNumber: Int
//): TvSeasonDetailsResponse
//
//@GET("search/multi")
//suspend fun fetchSearchResults(
//    @Query("query") query: String,
//    @Query("page") page: Int
//): MediaResponse