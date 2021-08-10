package com.example.movieland.data.models

import com.example.datasource.remote.models.responses.Result

data class HomeFeed(
    val title: String,
    val list: List<Result>
)