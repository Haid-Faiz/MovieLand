package com.example.movieland.ui.genres

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.MoviesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val moviesRepo: MoviesRepo
) : BaseViewModel(moviesRepo) {

//    lateinit var genresMedia: LiveData<PagingData<MovieResult>>

    fun getMediaByGenres(
        genresIds: String,
        isMovie: Boolean = true
    ) = if (isMovie)
        moviesRepo.fetchMoviesByGenres(genresIds = genresIds).cachedIn(viewModelScope)
    else
        moviesRepo.fetchTvShowsByGenres(genresIds = genresIds).cachedIn(viewModelScope)
}
