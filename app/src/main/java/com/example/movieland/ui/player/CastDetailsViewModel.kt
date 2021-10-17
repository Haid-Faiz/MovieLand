package com.example.movieland.ui.player

import androidx.lifecycle.*
import com.example.datasource.remote.models.responses.*
import com.example.movieland.BaseViewModel
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

//@HiltViewModel
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
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
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