package com.example.datasource.remote.api

import com.example.datasource.remote.models.requests.RequestToken
import com.example.datasource.remote.models.responses.AccessTokenResponse
import com.example.datasource.remote.models.responses.RequestTokenResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

// TMDB API Version - 4
interface TMDBApiServiceV4 {

    @POST("4/auth/request_token")
    @FormUrlEncoded
    suspend fun requestToken(
        @Field("redirect_to") redirectTo: String
    ): Response<RequestTokenResponse>


    // Create access token (successfully returns access token iff user has approved request token)
    @POST("4/auth/access_token")
    suspend fun requestAccessToken(
        @Body requestToken: RequestToken
    ): Response<AccessTokenResponse>
}


//
//@GET("movie/upcoming")
//suspend fun fetchUpcomingMovies(@Query("page") page: Int): PageResponse<Movie>
//
//@GET("movie/popular")
//suspend fun fetchPopularMovies(@Query("page") page: Int): PageResponse<Movie>
//
//@GET("movie/{id}?append_to_response=similar,videos")
//suspend fun fetchMovieDetails(@Path("id") movieId: Int): MovieDetailsResponse
//
//@GET("movie/{id}/similar")
//suspend fun fetchSimilarMovies(@Path("id") movieId: Int): PageResponse<Movie>
//
//@GET("movie/{id}/videos")
//suspend fun fetchMovieVideos(@Path("id") movieId: Int): VideosResponse
//
//@GET("tv/popular")
//suspend fun fetchPopularTvShows(@Query("page") page: Int): PageResponse<TvShow>
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