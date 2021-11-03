package com.example.movieland.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.AccessTokenResponse
import com.example.datasource.remote.models.responses.AccountDetailsResponse
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.datasource.remote.models.responses.MovieResult
import com.example.datasource.remote.models.responses.RequestTokenResponse
import com.example.datasource.remote.models.responses.SessionResponse
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.AuthRepo
import com.example.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : BaseViewModel(authRepo) {

    // Request Token
    private var _requestToken: MutableLiveData<Resource<RequestTokenResponse>> = MutableLiveData()
    var requestToken: LiveData<Resource<RequestTokenResponse>> = _requestToken

    // User Access Token
    private var _accessToken: MutableLiveData<Resource<AccessTokenResponse>> = MutableLiveData()
    var accessToken: LiveData<Resource<AccessTokenResponse>> = _accessToken

    // Session ID
    private var _sessionId: MutableLiveData<Resource<SessionResponse>> = MutableLiveData()
    var sessionId: LiveData<Resource<SessionResponse>> = _sessionId

    // User Account
    private var _userAccount: MutableLiveData<Resource<AccountDetailsResponse>> = MutableLiveData()
    var userAccount: LiveData<Resource<AccountDetailsResponse>> = _userAccount

    // WatchList
    private var _watchList: MutableLiveData<Resource<MutableList<MovieResult>>> = MutableLiveData()
    var watchList: LiveData<Resource<MutableList<MovieResult>>> = _watchList

    // Favourite List
    private var _favouriteList: MutableLiveData<Resource<MutableList<MovieResult>>> =
        MutableLiveData()
    var favouriteList: LiveData<Resource<MutableList<MovieResult>>> = _favouriteList

    // Rating List
    private var _ratingList: MutableLiveData<Resource<MutableList<MovieResult>>> = MutableLiveData()
    var ratingList: LiveData<Resource<MutableList<MovieResult>>> = _ratingList

    fun getUserDetail(sessionId: String) = viewModelScope.launch {
        _userAccount.postValue(Resource.Loading())
        _userAccount.postValue(authRepo.getUserDetail(sessionId = sessionId))
    }

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

    fun createSessionId(accessToken: String) = viewModelScope.launch {
        _sessionId.postValue(Resource.Loading())
        _sessionId.postValue(authRepo.createSession(accessToken = accessToken))
    }

    fun getFavouriteList(accountId: Int, sessionId: String) = viewModelScope.launch {
        _favouriteList.postValue(Resource.Loading())

        authRepo.getFavouriteMovies(
            accountId = accountId,
            sessionId = sessionId
        ).zip(
            authRepo.getFavouriteTvShows(
                accountId = accountId,
                sessionId = sessionId
            )
        ) { favouriteMoviesResponse: Response<MovieListResponse>, favouriteShowsResponse: Response<MovieListResponse> ->

            val favouriteMediaList = mutableListOf<MovieResult>()
            favouriteMediaList.addAll(favouriteMoviesResponse.body()!!.movieResults)
            favouriteMediaList.addAll(favouriteShowsResponse.body()!!.movieResults)
            return@zip favouriteMediaList
        }.flowOn(Dispatchers.Default)
            .catch { throwable ->
                when (throwable) {
                    is HttpException -> {
                        _favouriteList.postValue(
                            Resource.Error(
                                errorMessage = throwable.message ?: "Something went wrong"
                            )
                        )
                    }
                    is IOException -> _favouriteList.postValue(
                        Resource.Error(
                            errorMessage = "Please check your inernet connection"
                        )
                    )
                }
            }
            .collect {
                _favouriteList.postValue(Resource.Success(data = it))
            }
    }

    fun getWatchList(accountId: Int, sessionId: String) = viewModelScope.launch {
        _watchList.postValue(Resource.Loading())

        authRepo.getMoviesWatchList(
            accountId = accountId,
            sessionId = sessionId
        ).zip(
            authRepo.getTvShowsWatchList(
                accountId,
                sessionId
            )
        ) { movieWatchList, tvShowWatchList ->

            val watchList = mutableListOf<MovieResult>()
            watchList.addAll(movieWatchList.body()!!.movieResults)
            watchList.addAll(tvShowWatchList.body()!!.movieResults)
            watchList
        }.flowOn(Dispatchers.Default)
            .catch { throwable ->
                when (throwable) {
                    is HttpException -> _watchList.postValue(
                        Resource.Error(
                            errorMessage = "Something went wrong"
                        )
                    )

                    is IOException -> _watchList.postValue(
                        Resource.Error(
                            errorMessage = "Please check your internet connection"
                        )
                    )
                }
            }
            .collect {
                _watchList.postValue(Resource.Success(data = it))
            }
    }

    fun getRatedMovies(accountId: Int, sessionId: String) = viewModelScope.launch {
        _ratingList.postValue(Resource.Loading())

        authRepo.getRatedMovies(
            accountId = accountId,
            sessionId = sessionId
        ).zip(authRepo.getRatedTvShows(accountId = accountId, sessionId = sessionId)) { rM, rTv ->

            val ratingList = mutableListOf<MovieResult>()
            ratingList.addAll(rM.body()!!.movieResults)
            ratingList.addAll(rTv.body()!!.movieResults)
            ratingList
        }.flowOn(Dispatchers.Default)
            .catch { throwable ->
                when (throwable) {
                    is HttpException -> _ratingList.postValue(
                        Resource.Error(
                            errorMessage = throwable.message ?: "Something went wrong"
                        )
                    )

                    is IOException -> _ratingList.postValue(
                        Resource.Error(
                            errorMessage = "Please check your internet connection"
                        )
                    )
                }
            }
            .collect {
                _ratingList.postValue(Resource.Success(data = it))
            }
    }
}
