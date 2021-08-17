package com.example.movieland.ui.coming_soon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import kotlinx.coroutines.launch

class ComingSoonViewModel : ViewModel() {

    private val repo = MoviesRepo()

    private val _comingSoon = MutableLiveData<Resource<MovieListResponse>>()
    val comingSoon: LiveData<Resource<MovieListResponse>> = _comingSoon


    fun getComingSoonMovies() = viewModelScope.launch {
        _comingSoon.postValue(repo.fetchUpcomingMovies())
    }

}