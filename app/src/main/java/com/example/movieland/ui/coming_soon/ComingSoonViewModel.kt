package com.example.movieland.ui.coming_soon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComingSoonViewModel @Inject constructor(
    private val movieRepo: MoviesRepo
) : ViewModel() {

    private val _comingSoon = MutableLiveData<Resource<MovieListResponse>>()
    val comingSoon: LiveData<Resource<MovieListResponse>> = _comingSoon

    fun getComingSoonMovies() = viewModelScope.launch {
        _comingSoon.postValue(movieRepo.fetchUpcomingMovies())
    }

}