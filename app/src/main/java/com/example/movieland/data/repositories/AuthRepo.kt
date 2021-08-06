package com.example.movieland.data.repositories

import com.example.datasource.remote.ApiClient
import com.example.datasource.remote.api.TMDBApiService
import com.example.datasource.remote.models.requests.RequestToken

class AuthRepo : BaseRepo() {

    private val api = ApiClient().buildApi(TMDBApiService::class.java)

    suspend fun requestToken() = api.requestToken(
        "android://com.example.movieland/ui/auth/AuthActivity"
    ).body()?.requestToken!!


    suspend fun requestUserAccessToken(requestToken: String) = api.requestAccessToken(
        RequestToken(requestToken)
    ).body()

}