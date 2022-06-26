package com.codingcosmos.movieland.data.models

import com.codingcosmos.datasource.remote.models.responses.MovieResult

data class HomeFeedData(
    val bannerMovie: MovieResult,
    val homeFeedList: List<HomeFeed>
)

data class HomeFeed(
    val title: String,
    val list: List<MovieResult>
//    val viewModelFun: suspend () -> Resource<MovieListResponse>
)
