package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WatchProvidersResponse(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "results")
    var results: Results? = null
)
