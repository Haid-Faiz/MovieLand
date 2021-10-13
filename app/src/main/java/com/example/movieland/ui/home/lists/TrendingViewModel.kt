package com.example.movieland.ui.home.lists

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.MoviesRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

class TrendingViewModel @AssistedInject constructor(
    private val movieRepo: MoviesRepo,
    @Assisted
    private val isItMovie: Boolean
) : BaseViewModel(movieRepo) {

    @AssistedFactory
    interface TrendingViewModelFactory {
        fun create(isItMovie: Boolean): TrendingViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun providesFactory(
            assistedFactory: TrendingViewModelFactory,
            isItMovie: Boolean
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(isItMovie) as T
            }
        }
    }

    lateinit var trendingMedia: LiveData<PagingData<MovieResult>>

    init {
        if (isItMovie) getTrendingMovies() else getTrendingTvShows()
    }

    private fun getTrendingMovies() {
        trendingMedia = movieRepo.fetchTrendingMovies1().cachedIn(viewModelScope)
    }

    private fun getTrendingTvShows() {
        trendingMedia = movieRepo.fetchTrendingTvShows1().cachedIn(viewModelScope)
    }

}