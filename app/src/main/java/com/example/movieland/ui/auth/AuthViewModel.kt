package com.example.movieland.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.AccessTokenResponse
import com.example.datasource.remote.models.responses.RequestTokenResponse
import com.example.movieland.data.repositories.AuthRepo
import com.example.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    // Request Token
    private var _requestToken: MutableLiveData<Resource<RequestTokenResponse>> = MutableLiveData()
    var requestToken: LiveData<Resource<RequestTokenResponse>> = _requestToken

    // User Access Token
    private var _accessToken: MutableLiveData<Resource<AccessTokenResponse>> = MutableLiveData()
    var accessToken: LiveData<Resource<AccessTokenResponse>> = _accessToken

    fun requestToken() = viewModelScope.launch {
        _requestToken.postValue(Resource.Loading())
        _requestToken.postValue(authRepo.requestToken())
    }

    fun requestUserAccessToken(
        requestToken: String
    ) = viewModelScope.launch {
        _accessToken.postValue(Resource.Loading())
        _accessToken.postValue(authRepo.requestUserAccessToken(requestToken))
    }

}