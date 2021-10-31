package com.example.movieland.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.datasource.remote.api.TMDBApiServiceV3
import com.example.datasource.remote.models.responses.MovieListResponse
import com.example.datasource.remote.models.responses.MovieResult
import retrofit2.Response

class TrendingMediaPagingSource(
    val api: TMDBApiServiceV3,
    val mediaType: String
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchTrending(mediaType = mediaType, timeWindow = "day", page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    // The refresh key is used for subsequent refresh calls to PagingSource.load after the initial load
    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class NowPlayingPagingSource(
    val api: TMDBApiServiceV3
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchNowPlayingMovies(page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class TopRatedPagingSource(
    val api: TMDBApiServiceV3
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchTopRatedMovies(page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class PopularMoviesPagingSource(
    val api: TMDBApiServiceV3
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchPopularMovies(page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class PopularTvPagingSource(
    val api: TMDBApiServiceV3
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchPopularTvShows(page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class AnimePagingSource(
    val api: TMDBApiServiceV3
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchAnimeSeries(page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class BollywoodPagingSource(
    val api: TMDBApiServiceV3
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchBollywoodMovies(page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}


class GenresMoviesPagingSource(
    val api: TMDBApiServiceV3,
    private val genresIds: String
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchMoviesByGenres(genres = genresIds, page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page<Int, MovieResult>(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error<Int, MovieResult>(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class GenresTvShowsPagingSource(
    val api: TMDBApiServiceV3,
    private val genresIds: String
) : PagingSource<Int, MovieResult>() {

    private val STARTING_PAGE = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieResult> {

        val currentPage = params.key ?: STARTING_PAGE

        return try {

            val response: Response<MovieListResponse> =
                api.fetchTvShowsByGenres(genres = genresIds, page = currentPage)

            val moviesList: List<MovieResult> = response.body()!!.movieResults

            LoadResult.Page(
                data = moviesList,
                prevKey = if (currentPage == STARTING_PAGE) null else currentPage - 1,
                nextKey = if (moviesList.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}