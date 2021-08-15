package com.example.movieland.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repo = MoviesRepo()

    private val _trendingMovies = MutableLiveData<Resource<MovieListResponse>>()
    val trendingMovies: LiveData<Resource<MovieListResponse>> = _trendingMovies

    private val _searchedMovies = MutableLiveData<Resource<MovieListResponse>>()
    val searchedMovies: LiveData<Resource<MovieListResponse>> = _searchedMovies


    fun trendingMovies() = viewModelScope.launch {
        _trendingMovies.postValue(repo.fetchTrendingMovies())
    }

    fun searchedMovies(searchQuery: String) = viewModelScope.launch {
        _searchedMovies.postValue(repo.fetchSearchedMovies(searchQuery))
    }
}