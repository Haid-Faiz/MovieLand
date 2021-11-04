package com.example.movieland.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.datasource.remote.models.responses.TmdbErrorResponse
import com.example.movieland.BaseViewModel
import com.example.movieland.data.models.HomeFeed
import com.example.movieland.data.models.HomeFeedData
import com.example.movieland.data.repositories.MoviesRepo
import com.example.movieland.utils.Constants.ANIME_SERIES
import com.example.movieland.utils.Constants.BOLLYWOOD_MOVIES
import com.example.movieland.utils.Constants.NEWLY_LAUNCHED
import com.example.movieland.utils.Constants.POPULAR_MOVIES
import com.example.movieland.utils.Constants.POPULAR_TV_SHOWS
import com.example.movieland.utils.Constants.TOP_RATED_MOVIES
import com.example.movieland.utils.ErrorType
import com.example.movieland.utils.Resource
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepo: MoviesRepo
) : BaseViewModel(movieRepo) {

    private var _allFeedList: MutableLiveData<Resource<HomeFeedData>> = MutableLiveData()
    var allFeedList: LiveData<Resource<HomeFeedData>> = _allFeedList

    init {
        fetchAllFeedLists()
    }

    fun retry() = fetchAllFeedLists()

    private fun fetchAllFeedLists() = viewModelScope.launch {
        _allFeedList.postValue(Resource.Loading())

        try {
            coroutineScope {
                val nowPlayingMoviesListDef: Deferred<Resource<MovieListResponse>> =
                    async { movieRepo.fetchNowPlayingMovies() }
                val popularMoviesListDef = async { movieRepo.fetchPopularMovies() }
                val popularTvListDef = async { movieRepo.fetchPopularTvShows() }
                val topRatedMoviesListDef = async { movieRepo.fetchTopRatedMovies() }
                val animeSeriesDef = async { movieRepo.fetchAnimeSeries() }
                val bollywoodDef = async { movieRepo.fetchBollywoodMovies() }

                val wholeList = mutableListOf<HomeFeed>()

                // Now playing
                val nowPlayingMoviesList: Resource<MovieListResponse> =
                    nowPlayingMoviesListDef.await()
                // Popular Movies
                val popularMoviesList = popularMoviesListDef.await()
                // Popular Tv
                val popularTvList = popularTvListDef.await()
                // Top Rated
                val topRatedMoviesList = topRatedMoviesListDef.await()
                // Anime Series
                val animeSeriesList = animeSeriesDef.await()
                // Bollywood
                val bollywoodList = bollywoodDef.await()

                wholeList.add(HomeFeed(NEWLY_LAUNCHED, nowPlayingMoviesList.data!!.movieResults))
                wholeList.add(HomeFeed(POPULAR_MOVIES, popularMoviesList.data!!.movieResults))
                wholeList.add(HomeFeed(POPULAR_TV_SHOWS, popularTvList.data!!.movieResults))
                wholeList.add(HomeFeed(TOP_RATED_MOVIES, topRatedMoviesList.data!!.movieResults))
                wholeList.add(HomeFeed(ANIME_SERIES, animeSeriesList.data!!.movieResults))
                wholeList.add(HomeFeed(BOLLYWOOD_MOVIES, bollywoodList.data!!.movieResults))

                _allFeedList.postValue(
                    Resource.Success(
                        data = HomeFeedData(
                            bannerMovie = nowPlayingMoviesList.data.movieResults[
                                Random.nextInt(
                                    0,
                                    9
                                )
                            ],
                            homeFeedList = wholeList
                        )
                    )
                )
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    // val code = e.code() HTTP Exception code
                    val errorResponse: TmdbErrorResponse? = convertErrorBody(throwable)
                    _allFeedList.postValue(
                        Resource.Error(
                            errorMessage = errorResponse?.statusMessage ?: "Something went wrong",
                            errorType = ErrorType.HTTP
                        )
                    )
                }
                is IOException -> {
                    _allFeedList.postValue(
                        Resource.Error(
                            "Please check your internet connection",
                            errorType = ErrorType.NETWORK
                        )
                    )
                }
                else -> {
                    _allFeedList.postValue(
                        Resource.Error(
                            errorMessage = throwable.message ?: "Something went wrong",
                            errorType = ErrorType.UNKNOWN
                        )
                    )
                }
            }
        }
    }

    private fun convertErrorBody(throwable: Throwable): TmdbErrorResponse? {
        return try {
            (throwable as HttpException).response()?.errorBody()?.source()?.let {
                val moshiAdapter = Moshi.Builder().build().adapter(TmdbErrorResponse::class.java)
                moshiAdapter.fromJson(it)
            }
        } catch (exception: Exception) {
            null
        }
    }

    suspend fun addToWatchList(
        accountId: Int,
        sessionId: String,
        addToWatchListRequest: AddToWatchListRequest
    ): Resource<ResponseBody> {
        return movieRepo.addToWatchList(accountId, sessionId, addToWatchListRequest)
    }
}

//
// package com.example.movieland.ui.home
//
// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.viewModelScope
// import androidx.paging.PagingData
// import androidx.paging.cachedIn
// import com.example.datasource.remote.models.requests.AddToWatchListRequest
// import com.example.datasource.remote.models.responses.MovieListResponse
// import com.example.datasource.remote.models.responses.MovieResult
// import com.example.movieland.BaseViewModel
// import com.example.movieland.data.models.HomeFeed
// import com.example.movieland.data.models.HomeFeedData
// import com.example.movieland.data.repositories.MoviesRepo
// import com.example.movieland.utils.Resource
// import dagger.hilt.android.lifecycle.HiltViewModel
// import kotlinx.coroutines.*
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.flowOf
// import okhttp3.ResponseBody
// import retrofit2.Response
// import javax.inject.Inject
// import kotlin.random.Random
//
// @HiltViewModel
// class HomeViewModel @Inject constructor(
//    private val movieRepo: MoviesRepo
// ) : BaseViewModel(movieRepo) {
//
// //    private var _allFeedList: MutableLiveData<Resource<List<HomeFeed>>> = MutableLiveData()
// //    var allFeedList: LiveData<Resource<List<HomeFeed>>> = _allFeedList
//
//    private var _allFeedList: MutableLiveData<Resource<HomeFeedData>> = MutableLiveData()
//    var allFeedList: LiveData<Resource<HomeFeedData>> = _allFeedList
//
//    init {
//        fetchAllFeedLists()
//    }
//
//    private fun fetchAllFeedLists() = viewModelScope.launch {
//        _allFeedList.postValue(Resource.Loading())
//
//        try {
//            coroutineScope {
//
//                val nowPlayingMoviesListDef: Deferred<Flow<PagingData<MovieResult>>> =
//                    async { movieRepo.fetchNowPlayingMoviesPaging().cachedIn(viewModelScope) }
//                val popularMoviesListDef =
//                    async { movieRepo.fetchPopularMoviesPaging().cachedIn(viewModelScope) }
//                val popularTvListDef =
//                    async { movieRepo.fetchPopularTvShowsPaging().cachedIn(viewModelScope) }
//                val topRatedMoviesListDef =
//                    async { movieRepo.fetchTopRatedMoviesPaging().cachedIn(viewModelScope) }
//                val animeSeriesDef = async { movieRepo.fetchAnimeSeriesPaging().cachedIn(viewModelScope) }
//                val bollywoodDef =
//                    async { movieRepo.fetchBollywoodMoviesPaging().cachedIn(viewModelScope) }
//
//                val wholeList = mutableListOf<HomeFeed>()
//
//                // Now playing
//                val nowPlayingMoviesList: Flow<PagingData<MovieResult>> =
//                    nowPlayingMoviesListDef.await()
//                // Popular Movies
//                val popularMoviesList = popularMoviesListDef.await()
//                // Popular Tv
//                val popularTvList = popularTvListDef.await()
//                // Top Rated
//                val topRatedMoviesList = topRatedMoviesListDef.await()
//                // Anime Series
//                val animeSeriesList = animeSeriesDef.await()
//                // Bollywood
//                val bollywoodList = bollywoodDef.await()
//
//                wholeList.add(HomeFeed("Newly Launched", nowPlayingMoviesList))
//                wholeList.add(HomeFeed("Popular Movies", popularMoviesList))
//                wholeList.add(HomeFeed("Popular Tv Shows", popularTvList))
//                wholeList.add(HomeFeed("Top Rated Movies", topRatedMoviesList))
//                wholeList.add(HomeFeed("Anime Series", animeSeriesList))
//                wholeList.add(HomeFeed("Bollywood", bollywoodList))
//
//                val bannerMovieDef = async { movieRepo.fetchBannerMovie() }
//                // Banner
//                val bannerMovieResponse: Response<MovieListResponse> = bannerMovieDef.await()
//
//                val randomBannerMovie: MovieResult =
//                    bannerMovieResponse.body()!!.movieResults[Random.nextInt(0, 9)]
//
// //                _allFeedList.postValue(Resource.Success(data = wholeList))
//                _allFeedList.postValue(
//                    Resource.Success(
//                        data = HomeFeedData(
//                            bannerMovie = randomBannerMovie,
//                            homeFeedList = wholeList
//                        )
//                    )
//                )
//            }
//        } catch (e: Exception) {
//            _allFeedList.postValue(Resource.Error(message = e.message ?: "Something went wrong"))
//        }
//    }
//
//    suspend fun addToWatchList(
//        accountId: Int,
//        sessionId: String,
//        addToWatchListRequest: AddToWatchListRequest
//    ): Resource<ResponseBody> {
//        return movieRepo.addToWatchList(accountId, sessionId, addToWatchListRequest)
//    }
//
// }
