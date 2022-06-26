package com.codingcosmos.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// This class is coming in use for movies responses & tv show responses list

@JsonClass(generateAdapter = true)
data class MovieListResponse(
    @Json(name = "dates")
    var dates: Dates? = null,
    @Json(name = "page")
    var page: Int,
    @Json(name = "results")
    var movieResults: List<MovieResult>,
    @Json(name = "total_pages")
    var totalPages: Int,
    @Json(name = "total_results")
    var totalResults: Int
)

@JsonClass(generateAdapter = true)
data class Dates(
    @Json(name = "maximum")
    var maximum: String,
    @Json(name = "minimum")
    var minimum: String
)
