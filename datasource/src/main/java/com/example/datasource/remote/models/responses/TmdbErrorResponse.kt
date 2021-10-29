package com.example.datasource.remote.models.responses


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbErrorResponse(
    @Json(name = "status_code")
    var statusCode: Int,
    @Json(name = "status_message")
    var statusMessage: String,
    @Json(name = "success")
    var success: Boolean?
)