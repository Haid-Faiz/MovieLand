package com.codingcosmos.movieland.data.repositories

import com.codingcosmos.datasource.remote.api.TMDBApiServiceV3
import com.codingcosmos.datasource.remote.api.TMDBApiServiceV4
import com.codingcosmos.datasource.remote.models.requests.RequestToken
import com.codingcosmos.datasource.remote.models.responses.AccessTokenResponse
import com.codingcosmos.datasource.remote.models.responses.MovieListResponse
import com.codingcosmos.datasource.remote.models.responses.RequestTokenResponse
import com.codingcosmos.datasource.remote.models.responses.SessionResponse
import com.codingcosmos.movieland.utils.Constants.MOVIE_LAND_URL
import com.codingcosmos.movieland.utils.Resource
import com.codingcosmos.movieland.utils.SessionPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class AuthRepo @Inject constructor(
    private val apiV4: TMDBApiServiceV4,
    private val apiV3: TMDBApiServiceV3,
    private val sessionPrefs: SessionPrefs
) : BaseRepo(sessionPrefs) {

    suspend fun requestToken(): Resource<RequestTokenResponse> = safeApiCall {
        apiV4.requestToken(MOVIE_LAND_URL)
    }

    suspend fun requestUserAccessToken(
        requestToken: String
    ): Resource<AccessTokenResponse> = safeApiCall {
        apiV4.requestAccessToken(RequestToken(requestToken))
    }

    suspend fun createSession(
        accessToken: String
    ): Resource<SessionResponse> = safeApiCall {
        apiV3.createSessionIdFromV4(v4AccessToken = accessToken)
    }

    suspend fun getUserDetail(sessionId: String) = safeApiCall {
        apiV3.getAccountDetails(sessionId = sessionId)
    }

    fun getFavouriteMovies(
        accountId: Int,
        sessionId: String
    ): Flow<Response<MovieListResponse>> = flow<Response<MovieListResponse>> {
        emit(apiV3.getFavouriteMovies(accountId, sessionId))
    }

    fun getFavouriteTvShows(
        accountId: Int,
        sessionId: String
    ): Flow<Response<MovieListResponse>> = flow {
        emit(apiV3.getFavouriteTvShows(accountId = accountId, sessionId = sessionId))
    }

    fun getMoviesWatchList(
        accountId: Int,
        sessionId: String
    ): Flow<Response<MovieListResponse>> = flow {
        emit(apiV3.getMoviesWatchList(accountId = accountId, sessionId = sessionId))
    }

    fun getTvShowsWatchList(
        accountId: Int,
        sessionId: String
    ): Flow<Response<MovieListResponse>> = flow {
        emit(apiV3.getTvShowsWatchList(accountId = accountId, sessionId = sessionId))
    }

    fun getRatedMovies(
        accountId: Int,
        sessionId: String
    ): Flow<Response<MovieListResponse>> = flow {
        emit(apiV3.getRatedMovies(accountId = accountId, sessionId = sessionId))
    }

    fun getRatedTvShows(
        accountId: Int,
        sessionId: String
    ): Flow<Response<MovieListResponse>> = flow {
        emit(apiV3.getRatedTvShows(accountId = accountId, sessionId = sessionId))
    }
}
