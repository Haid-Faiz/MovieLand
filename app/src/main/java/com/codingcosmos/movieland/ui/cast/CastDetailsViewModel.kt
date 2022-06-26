package com.codingcosmos.movieland.ui.cast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codingcosmos.datasource.remote.models.responses.ActorFilmography
import com.codingcosmos.movieland.BaseViewModel
import com.codingcosmos.movieland.data.repositories.MoviesRepo
import com.codingcosmos.movieland.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

// @HiltViewModel
class CastDetailsViewModel @AssistedInject constructor(
    private val movieRepo: MoviesRepo,
    @Assisted private val personId: Int
) : BaseViewModel(movieRepo) {

    @AssistedFactory
    interface Factory {
        fun create(personId: Int): CastDetailsViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun providesFactory(
            assistedFactory: Factory,
            personId: Int
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(personId = personId) as T
            }
        }
    }

    private val _actorMoviesShows = MutableLiveData<Resource<ActorFilmography>>()
    val actorMoviesShows: LiveData<Resource<ActorFilmography>> = _actorMoviesShows

    init {
        getActorsMoviesShows(personId)
    }

    private fun getActorsMoviesShows(personId: Int) = viewModelScope.launch {
        _actorMoviesShows.postValue(Resource.Loading())
        _actorMoviesShows.postValue(movieRepo.fetchActorsMoviesShows(personId = personId))
    }
}
