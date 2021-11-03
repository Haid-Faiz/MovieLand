package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionResponse(
    @Json(name = "session_id")
    var sessionId: String,
    @Json(name = "success")
    var success: Boolean
)
