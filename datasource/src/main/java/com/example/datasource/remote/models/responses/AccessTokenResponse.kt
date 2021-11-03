package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccessTokenResponse(
    @Json(name = "access_token")
    var accessToken: String,
    @Json(name = "account_id")
    var accountId: String,
    @Json(name = "status_code")
    var statusCode: Int,
    @Json(name = "status_message")
    var statusMessage: String,
    @Json(name = "success")
    var success: Boolean
)
