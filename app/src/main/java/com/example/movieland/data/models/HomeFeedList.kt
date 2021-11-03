package com.example.movieland.data.models

import com.example.datasource.remote.models.responses.MovieResult

data class HomeFeedData(
    val bannerMovie: MovieResult,
    val homeFeedList: List<HomeFeed>
)

data class HomeFeed(
    val title: String,
    val list: List<MovieResult>
//    val viewModelFun: suspend () -> Resource<MovieListResponse>
)
