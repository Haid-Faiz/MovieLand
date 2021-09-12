package com.example.movieland.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.MovieDetailResponse
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val movieRepo: MoviesRepo
) : ViewModel() {

    private val _movieDetail = MutableLiveData<Resource<MovieDetailResponse>>()
    val movieDetail: LiveData<Resource<MovieDetailResponse>> = _movieDetail

    private val _similarMovies = MutableLiveData<Resource<MovieListResponse>>()
    val similarMovies: LiveData<Resource<MovieListResponse>> = _similarMovies

    fun getMovieDetail(movieId: Int) = viewModelScope.launch {
        _movieDetail.postValue(Resource.Loading())
        _movieDetail.postValue(movieRepo.fetchMovieDetail(movieId = movieId))
    }

    fun getSimilarMovies(movieId: Int) = viewModelScope.launch {
        _similarMovies.postValue(Resource.Loading())
        _similarMovies.postValue(movieRepo.fetchSimilarMovies(movieId = movieId))
    }

}