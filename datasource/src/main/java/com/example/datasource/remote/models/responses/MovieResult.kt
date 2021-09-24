package com.example.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// This class includes movie result + tv shows results

@JsonClass(generateAdapter = true)
data class MovieResult(
    @Json(name = "backdrop_path")
    var backdropPath: String? = null,  // it is sometimes coming null in search api call
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
    var posterPath: String? = null, // it is sometimes coming null in search api call
    @Json(name = "title")
    var title: String? = null,  // movies will have title & tv shows will have name
    @Json(name = "video")
    var isVideoAvailable: Boolean? = null,  // tv shows doesn't have this field
    @Json(name = "vote_average")
    var voteAverage: Double,
    @Json(name = "vote_count")
    var voteCount: Int,
    @Json(name = "rating")      // This field is only available in User Rated Movies/Shows Response
    var ratingByYou: Int? = null,
    @Json(name = "release_date")
    var releaseDate: String? = null,   // tv shows doesn't have this field
    @Json(name = "original_title")
    var originalTitle: String? = null,    // tv shows doesn't have this field
    @Json(name = "adult")
    var adult: Boolean? = null,    // tv shows doesn't have this field
    @Json(name = "name")
    var tvShowName: String? = null,
    @Json(name = "first_air_date")
    var tvShowFirstAirDate: String? = null,
    @Json(name = "original_name")
    var tvShowOriginalName: String? = null
)