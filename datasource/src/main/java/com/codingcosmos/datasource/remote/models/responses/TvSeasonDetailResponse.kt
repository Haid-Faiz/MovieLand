package com.codingcosmos.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TvSeasonDetailResponse(
    @Json(name = "air_date")
    var airDate: String?,
    @Json(name = "episodes")
    var episodes: List<Episode>,
    @Json(name = "_id")
    var _id: String,
    @Json(name = "id")
    var id: Int,
    @Json(name = "name")
    var name: String,
    @Json(name = "overview")
    var overview: String,
    @Json(name = "poster_path")
    var posterPath: String?,
    @Json(name = "season_number")
    var seasonNumber: Int,
    @Json(name = "videos")
    var videos: Videos
)
