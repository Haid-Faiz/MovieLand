package com.example.movieland.ui.search

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

@HiltViewModel // We need to remove this annotation if we want to use Assisted Injection
class SearchViewModel @Inject constructor(
    private val moviesRepo: MoviesRepo,
) : ViewModel() {

    var isMovie = true

    private val _trendingMedia = MutableLiveData<Resource<MovieListResponse>>()
    val trendingMedia: LiveData<Resource<MovieListResponse>> = _trendingMedia

    private val _searchedMedia = MutableLiveData<Resource<MovieListResponse>>()
    val searchedMedia: LiveData<Resource<MovieListResponse>> = _searchedMedia

    fun trendingMovies(isMovie: Boolean) = viewModelScope.launch {
        // This will fetch weekly trending data
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
