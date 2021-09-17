package com.example.datasource.remote.models.responses


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Episode(
    @Json(name = "air_date")
    var airDate: String,
    @Json(name = "crew")
    var crew: List<Crew>,
    @Json(name = "episode_number")
    var episodeNumber: Int,
    @Json(name = "guest_stars")
    var guestStars: List<GuestStar>,
    @Json(name = "id")
    var id: Int,
    @Json(name = "name")
    var name: String,
    @Json(name = "overview")
    var overview: String,
    @Json(name = "production_code")
    var productionCode: String,
    @Json(name = "season_number")
    var seasonNumber: Int,
    @Json(name = "still_path")
    var stillPath: String?,
    @Json(name = "vote_average")
    var voteAverage: Double,
    @Json(name = "vote_count")
    var voteCount: Int
)