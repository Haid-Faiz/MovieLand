package com.example.movieland.ui.genres

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.MoviesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val moviesRepo: MoviesRepo
) : BaseViewModel(moviesRepo) {

//    var genresMedia: LiveData<PagingData<MovieResult>> ? = null

    fun getMediaByGenres(
        genresIds: String,
        isMovie: Boolean = true
    ): LiveData<PagingData<MovieResult>> {
        return if (isMovie)
            moviesRepo.fetchMoviesByGenres(genresIds = genresIds).cachedIn(viewModelScope)
        else
            moviesRepo.fetchTvShowsByGenres(genresIds = genresIds).cachedIn(viewModelScope)
    }

}