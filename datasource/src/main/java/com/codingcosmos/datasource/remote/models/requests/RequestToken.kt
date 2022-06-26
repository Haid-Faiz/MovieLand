package com.codingcosmos.datasource.remote.models.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestToken(
    @Json(name = "request_token")
    var requestToken: String
)
