package com.example.movieland.ui.genres

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val moviesRepo: MoviesRepo
) : BaseViewModel(moviesRepo) {

    private var _genresMedia: MutableLiveData<Resource<MovieListResponse>> = MutableLiveData()
    val genresMedia: LiveData<Resource<MovieListResponse>> = _genresMedia

    fun getMediaByGenres(genresIds: String, isMovie: Boolean = true) = viewModelScope.launch {
        _genresMedia.postValue(Resource.Loading())
        if (isMovie)
            _genresMedia.postValue(moviesRepo.fetchMoviesByGenres(genresIds = genresIds))
        else
            _genresMedia.postValue(moviesRepo.fetchTvShowsByGenres(genresIds = genresIds))
    }

}