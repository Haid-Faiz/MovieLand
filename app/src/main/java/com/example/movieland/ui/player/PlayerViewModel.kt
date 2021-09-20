package com.example.movieland.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.requests.AddToFavouriteRequest
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.requests.MediaRatingRequest
import com.example.datasource.remote.models.responses.MovieDetailResponse
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.datasource.remote.models.responses.TvSeasonDetailResponse
import com.example.datasource.remote.models.responses.TvShowDetailsResponse
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val movieRepo: MoviesRepo
) : BaseViewModel(movieRepo) {

    private val _movieDetail = MutableLiveData<Resource<MovieDetailResponse>>()
    val movieDetail: LiveData<Resource<MovieDetailResponse>> = _movieDetail

    private val _tvShowDetail = MutableLiveData<Resource<TvShowDetailsResponse>>()
    val tvShowDetail: LiveData<Resource<TvShowDetailsResponse>> = _tvShowDetail

    private val _similarMovies = MutableLiveData<Resource<MovieListResponse>>()
    val similarMovies: LiveData<Resource<MovieListResponse>> = _similarMovies

    private val _tvSeasonDetail = MutableLiveData<Resource<TvSeasonDetailResponse>>()
    val tvSeasonDetail: LiveData<Resource<TvSeasonDetailResponse>> = _tvSeasonDetail

    fun getMovieDetail(movieId: Int) = viewModelScope.launch {
        _movieDetail.postValue(Resource.Loading())
        _movieDetail.postValue(movieRepo.fetchMovieDetail(movieId = movieId))
    }

    fun getTvShowDetail(tvId: Int) = viewModelScope.launch {
        _tvShowDetail.postValue(Resource.Loading())
        _tvShowDetail.postValue(movieRepo.fetchTvShowDetail(tvId = tvId))
    }

    fun getSimilarMovies(movieId: Int) = viewModelScope.launch {
        _similarMovies.postValue(Resource.Loading())
        _similarMovies.postValue(movieRepo.fetchSimilarMovies(movieId = movieId))
    }

    fun getSimilarShows(tvId: Int) = viewModelScope.launch {
        _similarMovies.postValue(Resource.Loading())
        _similarMovies.postValue(movieRepo.fetchSimilarShows(tvID = tvId))
    }

    fun getTvSeasonDetail(tvId: Int, seasonNumber: Int) = viewModelScope.launch {
        _tvSeasonDetail.postValue(Resource.Loading())
        _tvSeasonDetail.postValue(
            movieRepo.fetchTvSeasonDetails(tvId = tvId, seasonNumber = seasonNumber)
        )
    }

    //---------------------------Session Id is required in these requests-----------------------

    suspend fun rateMovie(
        movieId: Int,
        sessionId: String,
        mediaRatingRequest: MediaRatingRequest
    ): Resource<ResponseBody> {
        return movieRepo.rateMovie(movieId, sessionId, mediaRatingRequest)
    }

    suspend fun rateTvShow(
        tvId: Int,
        sessionId: String,
        mediaRatingRequest: MediaRatingRequest
    ): Resource<ResponseBody> {
        return movieRepo.rateTvShow(tvId, sessionId, mediaRatingRequest)
    }


    suspend fun addToWatchList(
        accountId: Int,
        sessionId: String,
        addToWatchListRequest: AddToWatchListRequest
    ): Resource<ResponseBody> {
        return movieRepo.addToWatchList(accountId, sessionId, addToWatchListRequest)
    }

    suspend fun addToFavourites(
        accountId: Int,
        sessionId: String,
        addToFavouriteRequest: AddToFavouriteRequest
    ): Resource<ResponseBody> {
        return movieRepo.addToFavourites(accountId, sessionId, addToFavouriteRequest)
    }


}