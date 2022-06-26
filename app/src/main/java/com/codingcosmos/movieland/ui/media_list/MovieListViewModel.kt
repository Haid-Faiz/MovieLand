package com.codingcosmos.movieland.ui.media_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.codingcosmos.datasource.remote.models.responses.MovieResult
import com.codingcosmos.movieland.BaseViewModel
import com.codingcosmos.movieland.data.repositories.MoviesRepo
import com.codingcosmos.movieland.utils.Constants.ANIME_SERIES
import com.codingcosmos.movieland.utils.Constants.BOLLYWOOD_MOVIES
import com.codingcosmos.movieland.utils.Constants.NEWLY_LAUNCHED
import com.codingcosmos.movieland.utils.Constants.POPULAR_MOVIES
import com.codingcosmos.movieland.utils.Constants.POPULAR_TV_SHOWS
import com.codingcosmos.movieland.utils.Constants.TOP_RATED_MOVIES
import com.codingcosmos.movieland.utils.Constants.TRENDING_MOVIES
import com.codingcosmos.movieland.utils.Constants.TRENDING_TV_SHOWS
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MovieListViewModel @AssistedInject constructor(
    private val movieRepo: MoviesRepo,
    @Assisted
    private val mediaCategory: String
) : BaseViewModel(movieRepo) {

    @AssistedFactory
    interface TrendingViewModelFactory {
        fun create(mediaCategory: String): MovieListViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun providesFactory(
            assistedFactory: TrendingViewModelFactory,
            mediaCategory: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(mediaCategory) as T
            }
        }
    }

    lateinit var categoryWiseMediaList: LiveData<PagingData<MovieResult>>

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
        categoryWiseMediaList = movieRepo.fetchTrendingMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getTrendingTvShows() {
        categoryWiseMediaList = movieRepo.fetchTrendingTvShowsPaging().cachedIn(viewModelScope)
    }

    private fun getNewlyLaunchedMovies() {
        categoryWiseMediaList = movieRepo.fetchNowPlayingMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getPopularMovies() {
        categoryWiseMediaList = movieRepo.fetchPopularMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getPopularTvShows() {
        categoryWiseMediaList = movieRepo.fetchPopularTvShowsPaging().cachedIn(viewModelScope)
    }

    private fun getTopRatedMovies() {
        categoryWiseMediaList = movieRepo.fetchTopRatedMoviesPaging().cachedIn(viewModelScope)
    }

    private fun getAnimeSeries() {
        categoryWiseMediaList = movieRepo.fetchAnimeSeriesPaging().cachedIn(viewModelScope)
    }

    private fun getBollywoodMovies() {
        categoryWiseMediaList = movieRepo.fetchBollywoodMoviesPaging().cachedIn(viewModelScope)
    }
}
