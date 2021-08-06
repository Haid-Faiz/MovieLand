package com.example.movieland.ui.auth

import androidx.lifecycle.ViewModel
import com.example.movieland.data.repositories.AuthRepo

class AuthViewModel : ViewModel() {


    private val authRepo = AuthRepo()

    suspend fun requestToken() = authRepo.requestToken()

    suspend fun requestUserAccessToken(requestToken: String) = authRepo.requestUserAccessToken(requestToken)
}