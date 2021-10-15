package com.example.movieland.ui.search

import androidx.lifecycle.*
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Constants.MOVIE
import com.example.movieland.utils.Constants.TV
import com.example.movieland.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel  // We need to remove this annotation if we want to use Assisted Injection
class SearchViewModel @Inject constructor(
    private val moviesRepo: MoviesRepo,
) : ViewModel() {

    var isMovie = true

    private val _trendingMedia = MutableLiveData<Resource<MovieListResponse>>()
    val trendingMedia: LiveData<Resource<MovieListResponse>> = _trendingMedia

    private val _searchedMedia = MutableLiveData<Resource<MovieListResponse>>()
    val searchedMedia: LiveData<Resource<MovieListResponse>> = _searchedMedia

    fun trendingMovies(isMovie: Boolean) = viewModelScope.launch {
        _trendingMedia.postValue(Resource.Loading())
        if (isMovie)
            _trendingMedia.postValue(moviesRepo.fetchTrendingMovies())
        else
            _trendingMedia.postValue(moviesRepo.fetchTrendingTvShows())
    }

    fun searchMedia(isMovie: Boolean, searchQuery: String) = viewModelScope.launch {
        _searchedMedia.postValue(Resource.Loading())
        if (isMovie)
            _searchedMedia.postValue(moviesRepo.fetchSearchedMovies(searchQuery))
        else
            _searchedMedia.postValue(moviesRepo.fetchSearchedTvShows(searchQuery))
    }
}