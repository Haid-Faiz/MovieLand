package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoResult(
    @Json(name = "id")
    var id: String,
    @Json(name = "key")
    var key: String,
    @Json(name = "name")
    var name: String,
    @Json(name = "official")
    var official: Boolean,
    @Json(name = "published_at")
    var publishedAt: String,
    @Json(name = "site")
    var site: String,
    @Json(name = "size")
    var size: Int,
    @Json(name = "type")
    var type: String
)
