package com.example.movieland.data.repositories

import com.example.datasource.remote.api.TMDBApiServiceV4
import com.example.datasource.remote.models.requests.RequestToken
import com.example.datasource.remote.models.responses.AccessTokenResponse
import com.example.datasource.remote.models.responses.RequestTokenResponse
import com.example.movieland.utils.Constants.MOVIE_LAND_URL
import com.example.movieland.utils.Resource
import com.example.movieland.utils.SessionPrefs
import javax.inject.Inject

class AuthRepo @Inject constructor(
    private val apiV4: TMDBApiServiceV4,
    private val sessionPrefs: SessionPrefs
) : BaseRepo(sessionPrefs) {

//    private val api = ApiClient().buildApi(TMDBApiServiceV4::class.java)

    suspend fun requestToken(): Resource<RequestTokenResponse> =
        safeApiCall { apiV4.requestToken(MOVIE_LAND_URL) }


    suspend fun requestUserAccessToken(requestToken: String): Resource<AccessTokenResponse> =
        safeApiCall {
            apiV4.requestAccessToken(RequestToken(requestToken))
        }
}