package com.example.movieland.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val movieRepo = MoviesRepo()

    private var _movieListNowPlaying: MutableLiveData<Resource<MovieListResponse>> =
        MutableLiveData()
    var movieListNowPlaying: LiveData<Resource<MovieListResponse>> = _movieListNowPlaying

    private var _movieListUpcoming: MutableLiveData<Resource<MovieListResponse>> = MutableLiveData()
    var movieListUpcoming: LiveData<Resource<MovieListResponse>> = _movieListUpcoming

    private var _movieListTopRated: MutableLiveData<Resource<MovieListResponse>> = MutableLiveData()
    var movieListTopRated: LiveData<Resource<MovieListResponse>> = _movieListTopRated

    private var _movieListPopular: MutableLiveData<Resource<MovieListResponse>> = MutableLiveData()
    var movieListPopular: LiveData<Resource<MovieListResponse>> = _movieListPopular

    private var _tvListPopular: MutableLiveData<Resource<MovieListResponse>> = MutableLiveData()
    var tvListPopular: LiveData<Resource<MovieListResponse>> = _tvListPopular


    fun getNowPlayingMovies() = viewModelScope.launch {
        _movieListNowPlaying.postValue(movieRepo.fetchNowPlayingMovies())
    }

    fun getTopRatedMovies() = viewModelScope.launch {
        _movieListTopRated.postValue(movieRepo.fetchTopRatedMovies())
    }

    fun getUpcomingMovies() = viewModelScope.launch {
        _movieListUpcoming.postValue(movieRepo.fetchUpcomingMovies())
    }

    fun getPopularMovies() = viewModelScope.launch {
        _movieListUpcoming.postValue(movieRepo.fetchPopularMovies())
    }

    fun getPopularTvShows() = viewModelScope.launch {
        _movieListUpcoming.postValue(movieRepo.fetchPopularTvShows())
    }

    fun getTrendingTvShows() = viewModelScope.launch {
        _movieListUpcoming.postValue(movieRepo.fetchTrendingTvShows())
    }

    fun getTrendingMovies() = viewModelScope.launch {
        _movieListUpcoming.postValue(movieRepo.fetchTrendingMovies())
    }

}