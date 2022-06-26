package com.codingcosmos.movieland.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.codingcosmos.datasource.remote.models.requests.AddToFavouriteRequest
import com.codingcosmos.datasource.remote.models.requests.AddToWatchListRequest
import com.codingcosmos.datasource.remote.models.requests.MediaRatingRequest
import com.codingcosmos.datasource.remote.models.responses.MediaCastResponse
import com.codingcosmos.datasource.remote.models.responses.MovieDetailResponse
import com.codingcosmos.datasource.remote.models.responses.MovieListResponse
import com.codingcosmos.datasource.remote.models.responses.TvSeasonDetailResponse
import com.codingcosmos.datasource.remote.models.responses.TvShowDetailsResponse
import com.codingcosmos.datasource.remote.models.responses.TvShowExternalIdsResponse
import com.codingcosmos.datasource.remote.models.responses.WatchProvidersResponse
import com.codingcosmos.movieland.BaseViewModel
import com.codingcosmos.movieland.data.repositories.MoviesRepo
import com.codingcosmos.movieland.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val movieRepo: MoviesRepo
) : BaseViewModel(movieRepo) {

    private val _movieDetail = MutableLiveData<Resource<MovieDetailResponse>>()
    val movieDetail: LiveData<Resource<MovieDetailResponse>> = _movieDetail

    private val _tvShowDetail = MutableLiveData<Resource<TvShowDetailsResponse>>()
    val tvShowDetail: LiveData<Resource<TvShowDetailsResponse>> = _tvShowDetail

    private val _similarMedia = MutableLiveData<Resource<MovieListResponse>>()
    val similarMedia: LiveData<Resource<MovieListResponse>> = _similarMedia

    private val _recommendedMedia = MutableLiveData<Resource<MovieListResponse>>()
    val recommendedMedia: LiveData<Resource<MovieListResponse>> = _recommendedMedia

    private val _mediaCast = MutableLiveData<Resource<MediaCastResponse>>()
    val mediaCast: LiveData<Resource<MediaCastResponse>> = _mediaCast

    private val _tvSeasonDetail = MutableLiveData<Resource<TvSeasonDetailResponse>>()
    val tvSeasonDetail: LiveData<Resource<TvSeasonDetailResponse>> = _tvSeasonDetail

    private val _watchProviders = MutableLiveData<Resource<WatchProvidersResponse>>()
    val watchProviders: LiveData<Resource<WatchProvidersResponse>> = _watchProviders

    fun getMovieDetail(movieId: Int) = viewModelScope.launch {
        _movieDetail.postValue(Resource.Loading())
        _movieDetail.postValue(movieRepo.fetchMovieDetail(movieId = movieId))
    }

    fun getTvShowDetail(tvId: Int) = viewModelScope.launch {
        _tvShowDetail.postValue(Resource.Loading())
        _tvShowDetail.postValue(movieRepo.fetchTvShowDetail(tvId = tvId))
    }

    fun getSimilarMedia(mediaId: Int, isItMovie: Boolean) = viewModelScope.launch {
        _similarMedia.postValue(Resource.Loading())
        if (isItMovie)
            _similarMedia.postValue(movieRepo.fetchSimilarMovies(movieId = mediaId))
        else
            _similarMedia.postValue(movieRepo.fetchSimilarShows(tvID = mediaId))
    }

    fun getRecommendationsMedia(mediaId: Int, isItMovie: Boolean) = viewModelScope.launch {
        _recommendedMedia.postValue(Resource.Loading())
        if (isItMovie)
            _recommendedMedia.postValue(movieRepo.fetchRecommendedMovies(movieId = mediaId))
        else
            _recommendedMedia.postValue(movieRepo.fetchRecommendedTvShows(tvID = mediaId))
    }

    fun getTvSeasonDetail(tvId: Int, seasonNumber: Int) = viewModelScope.launch {
        _tvSeasonDetail.postValue(Resource.Loading())
        _tvSeasonDetail.postValue(
            movieRepo.fetchTvSeasonDetails(tvId = tvId, seasonNumber = seasonNumber)
        )
    }

    fun getMediaCast(mediaId: Int, isItMovie: Boolean) = viewModelScope.launch {
        if (isItMovie)
            _mediaCast.postValue(movieRepo.fetchMovieCast(movieId = mediaId))
        else
            _mediaCast.postValue(movieRepo.fetchTvShowCast(tvId = mediaId))
    }

    suspend fun getTvShowExternalIds(
        tvId: Int
    ): Resource<TvShowExternalIdsResponse> {
        return movieRepo.getTvShowExternalIds(tvId)
    }

    fun getMovieWatchProviders(
        movieId: Int
    ) = viewModelScope.launch {
        _watchProviders.postValue(movieRepo.getMovieWatchProviders(movieId))
    }

    fun getTvWatchProviders(
        tvId: Int
    ) = viewModelScope.launch {
        _watchProviders.postValue(movieRepo.getTvWatchProviders(tvId))
    }

    // ---------------------------Session Id is required in these requests-----------------------

    suspend fun rateMedia(
        mediaId: Int,
        isMovie: Boolean,
        sessionId: String,
        mediaRatingRequest: MediaRatingRequest
    ): Resource<ResponseBody> = if (isMovie)
        movieRepo.rateMovie(movieId = mediaId, sessionId, mediaRatingRequest)
    else
        movieRepo.rateTvShow(tvId = mediaId, sessionId, mediaRatingRequest)

    suspend fun addToWatchList(
        accountId: Int,
        sessionId: String,
        addToWatchListRequest: AddToWatchListRequest
    ): Resource<ResponseBody> {
        return movieRepo.addToWatchList(accountId, sessionId, addToWatchListRequest)
    }

    suspend fun addToFavourites(
        accountId: Int,
        sessionId: String,
        addToFavouriteRequest: AddToFavouriteRequest
    ): Resource<ResponseBody> {
        return movieRepo.addToFavourites(accountId, sessionId, addToFavouriteRequest)
    }
}
