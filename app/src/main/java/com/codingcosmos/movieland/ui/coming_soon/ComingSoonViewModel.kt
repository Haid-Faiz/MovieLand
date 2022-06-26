package com.codingcosmos.movieland.ui.coming_soon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.codingcosmos.datasource.remote.models.requests.AddToWatchListRequest
import com.codingcosmos.datasource.remote.models.responses.MovieListResponse
import com.codingcosmos.movieland.BaseViewModel
import com.codingcosmos.movieland.data.repositories.MoviesRepo
import com.codingcosmos.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class ComingSoonViewModel @Inject constructor(
    private val movieRepo: MoviesRepo
) : BaseViewModel(movieRepo) {

    private val _comingSoon = MutableLiveData<Resource<MovieListResponse>>()
    val comingSoon: LiveData<Resource<MovieListResponse>> = _comingSoon

    init {
        getComingSoonMovies()
    }

    fun retry() = getComingSoonMovies()

    private fun getComingSoonMovies() = viewModelScope.launch {
        _comingSoon.postValue(Resource.Loading())
        _comingSoon.postValue(movieRepo.fetchUpcomingMovies())
    }

    suspend fun addToWatchList(
        accountId: Int,
        sessionId: String,
        addToWatchListRequest: AddToWatchListRequest
    ): Resource<ResponseBody> {
        return movieRepo.addToWatchList(accountId, sessionId, addToWatchListRequest)
    }
}
