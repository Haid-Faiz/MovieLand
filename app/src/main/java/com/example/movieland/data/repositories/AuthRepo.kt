package com.example.movieland.data.repositories

import com.example.datasource.remote.ApiClient
import com.example.datasource.remote.api.TMDBApiService
import com.example.datasource.remote.models.requests.RequestToken
import com.example.datasource.remote.models.responses.AccessTokenResponse
import com.example.movieland.utils.Constants.MOVIE_LAND_URL
import com.example.movieland.utils.Resource

class AuthRepo : BaseRepo() {

    private val api = ApiClient().buildApi(TMDBApiService::class.java)

    suspend fun requestToken() = safeApiCall { api.requestToken(MOVIE_LAND_URL) }


    suspend fun requestUserAccessToken(requestToken: String): Resource<AccessTokenResponse> =
        safeApiCall {
            api.requestAccessToken(RequestToken(requestToken))
        }

}