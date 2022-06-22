package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TvShowExternalIdsResponse(
    @Json(name = "imdb_id")
    var imdbId: String? = null
)
