package com.codingcosmos.datasource.remote.models.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddToWatchListRequest(
    @Json(name = "media_id")
    var mediaId: Int,
    @Json(name = "media_type")
    var mediaType: String,
    @Json(name = "watchlist")
    var watchlist: Boolean
)
