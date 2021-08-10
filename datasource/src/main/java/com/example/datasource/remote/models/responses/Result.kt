package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// This class includes movie result + tv shows results

@JsonClass(generateAdapter = true)
data class Result(
    @Json(name = "backdrop_path")
    var backdropPath: String,
    @Json(name = "genre_ids")
    var genreIds: List<Int>,
    @Json(name = "id")
    var id: Int,
    @Json(name = "media_type")
    var mediaType: String? = null, // getting this extra value in trending list: movie, show, etc
    @Json(name = "original_language")
    var originalLanguage: String,
    @Json(name = "overview")
    var overview: String,
    @Json(name = "popularity")
    var popularity: Double,
    @Json(name = "poster_path")
    var posterPath: String,
    @Json(name = "title")
    var title: String,
    @Json(name = "video")
    var isVideoAvailable: Boolean,
    @Json(name = "vote_average")
    var voteAverage: Double,
    @Json(name = "vote_count")
    var voteCount: Int,
    @Json(name = "release_date")
    var releaseDate: String,
    @Json(name = "original_title")
    var originalTitle: String,
    @Json(name = "adult")
    var adult: Boolean,
    @Json(name = "name")
    var tvShowName: String? = null,
    @Json(name = "first_air_date")
    var tvShowFirstAirDate: String? = null,
    @Json(name = "original_name")
    var tvShowOriginalName: String? = null
)

