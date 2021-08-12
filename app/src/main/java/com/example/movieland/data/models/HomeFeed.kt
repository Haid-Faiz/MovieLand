package com.example.movieland.data.models

import com.example.datasource.remote.models.responses.MovieResult

data class HomeFeed(
    val title: String,
    val list: List<MovieResult>
)