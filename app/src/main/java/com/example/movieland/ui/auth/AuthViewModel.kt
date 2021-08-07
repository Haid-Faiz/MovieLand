package com.example.movieland.ui.auth

import androidx.lifecycle.ViewModel
import com.example.datasource.remote.models.responses.AccessTokenResponse
import com.example.datasource.remote.models.responses.RequestTokenResponse
import com.example.movieland.data.repositories.AuthRepo
import com.example.movieland.utils.Resource

class AuthViewModel : ViewModel() {

    private val authRepo = AuthRepo()

    suspend fun requestToken(): Resource<RequestTokenResponse> = authRepo.requestToken()

    suspend fun requestUserAccessToken(
        requestToken: String
    ): Resource<AccessTokenResponse> = authRepo.requestUserAccessToken(requestToken)

}