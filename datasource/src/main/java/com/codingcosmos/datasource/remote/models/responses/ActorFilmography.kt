package com.codingcosmos.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActorFilmography(
    @Json(name = "cast")
    var cast: List<MovieResult>,
//    @Json(name = "crew")
//    var crew: List<Crew>,
    @Json(name = "id")
    var id: Int
)
