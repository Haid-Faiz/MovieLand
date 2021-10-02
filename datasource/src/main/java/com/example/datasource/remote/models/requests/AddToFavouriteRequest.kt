package com.example.datasource.remote.models.requests


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddToFavouriteRequest(
    @Json(name = "favorite")
    var favorite: Boolean,
    @Json(name = "media_id")
    var mediaId: Int,
    @Json(name = "media_type")
    var mediaType: String
)