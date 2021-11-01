package com.example.movieland.ui.media_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Constants.ANIME_SERIES
import com.example.movieland.utils.Constants.BOLLYWOOD_MOVIES
import com.example.movieland.utils.Constants.NEWLY_LAUNCHED
import com.example.movieland.utils.Constants.POPULAR_MOVIES
import com.example.movieland.utils.Constants.POPULAR_TV_SHOWS
import com.example.movieland.utils.Constants.TOP_RATED_MOVIES
import com.example.movieland.utils.Constants.TRENDING_MOVIES
import com.example.movieland.utils.Constants.TRENDING_TV_SHOWS
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class TrendingViewModel @AssistedInject constructor(
    private val movieRepo: MoviesRepo,
    @Assisted
    private val mediaCategory: String
) : BaseViewModel(movieRepo) {

    @AssistedFactory
    interface TrendingViewModelFactory {
        fun create(mediaCategory: String): TrendingViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun providesFactory(
            assistedFactory: TrendingViewModelFactory,
            mediaCategory: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(mediaCategory) as T
            }
        }
    }

    lateinit var categoryMedia: LiveData<PagingData<MovieResult>>

    init {
        when (mediaCategory) {
            TRENDING_MOVIES -> getTrendingMovies()
            TRENDING_TV_SHOWS -> getTrendingTvShows()
            NEWLY_LAUNCHED -> getNewlyLaunchedMovies()
            POPULAR_MOVIES -> getPopularMovies()
            POPULAR_TV_SHOWS -> getPopularTvShows()
            TOP_RATED_MOVIES -> getTopRatedMovies()
            ANIME_SERIES -> getAnimeSeries()
            BOLLYWOOD_MOVIES -> getBollywoodMovies()
        }
    }

    private fun getTrendingMovies() {
        categoryMedia = movieRepo.fetchTrendingMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getTrendingTvShows() {
        categoryMedia = movieRepo.fetchTrendingTvShowsPaging().cachedIn(viewModelScope)
    }

    private fun getNewlyLaunchedMovies() {
        categoryMedia = movieRepo.fetchNowPlayingMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getPopularMovies() {
        categoryMedia = movieRepo.fetchPopularMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getPopularTvShows() {
        categoryMedia = movieRepo.fetchPopularTvShowsPaging().cachedIn(viewModelScope)
    }

    private fun getTopRatedMovies() {
        categoryMedia = movieRepo.fetchTopRatedMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getAnimeSeries() {
        categoryMedia = movieRepo.fetchAnimeSeriesPaging().cachedIn(viewModelScope)
    }

    private fun getBollywoodMovies() {
        categoryMedia = movieRepo.fetchBollywoodMoviesPaging().cachedIn(viewModelScope)
    }
}
