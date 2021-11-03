package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TvShowDetailsResponse(
    @Json(name = "backdrop_path")
    var backdropPath: String,
    @Json(name = "created_by")
    var createdBy: List<Any>,
    @Json(name = "episode_run_time")
    var episodeRunTime: List<Int>,
    @Json(name = "first_air_date")
    var firstAirDate: String?,
    @Json(name = "genres")
    var genres: List<Genre>,
    @Json(name = "homepage")
    var homepage: String,
    @Json(name = "id")
    var id: Int,
    @Json(name = "in_production")
    var inProduction: Boolean,
    @Json(name = "languages")
    var languages: List<String>,
    @Json(name = "last_air_date")
    var lastAirDate: String?,
    @Json(name = "last_episode_to_air")
    var lastEpisodeToAir: LastEpisodeToAir?,
    @Json(name = "name")
    var name: String,
    @Json(name = "next_episode_to_air")
    var nextEpisodeToAir: NextEpisodeToAir?,
    @Json(name = "number_of_episodes")
    var numberOfEpisodes: Int,
    @Json(name = "number_of_seasons")
    var numberOfSeasons: Int,
    @Json(name = "original_name")
    var originalName: String,
    @Json(name = "overview")
    var overview: String,
    @Json(name = "popularity")
    var popularity: Double,
    @Json(name = "poster_path")
    var posterPath: String,
    @Json(name = "seasons")
    var seasons: List<Season>,
    @Json(name = "status")
    var status: String,
    @Json(name = "tagline")
    var tagline: String,
    @Json(name = "type")
    var type: String,
    @Json(name = "videos")
    var videos: Videos,
    @Json(name = "vote_average")
    var voteAverage: Double,
    @Json(name = "vote_count")
    var voteCount: Int
)

@JsonClass(generateAdapter = true)
data class LastEpisodeToAir(
    @Json(name = "air_date")
    var airDate: String?,
    @Json(name = "episode_number")
    var episodeNumber: Int,
    @Json(name = "id")
    var id: Int,
    @Json(name = "name")
    var name: String,
    @Json(name = "overview")
    var overview: String,
    @Json(name = "production_code")
    var productionCode: String?,
    @Json(name = "season_number")
    var seasonNumber: Int,
    @Json(name = "still_path")
    var stillPath: String?,
    @Json(name = "vote_average")
    var voteAverage: Double,
    @Json(name = "vote_count")
    var voteCount: Int
)

@JsonClass(generateAdapter = true)
data class NextEpisodeToAir(
    @Json(name = "air_date")
    var airDate: String?,
    @Json(name = "episode_number")
    var episodeNumber: Int,
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
