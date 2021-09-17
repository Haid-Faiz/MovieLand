package com.example.datasource.remote.models.responses


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieDetailResponse(
    @Json(name = "adult")
    var adult: Boolean,
    @Json(name = "backdrop_path")
    var backdropPath: String,
    @Json(name = "belongs_to_collection")
    var belongsToCollection: Any?,
    @Json(name = "budget")
    var budget: Int,
    @Json(name = "genres")
    var genres: List<Genre>,
    @Json(name = "homepage")
    var homepage: String,
    @Json(name = "id")
    var id: Int,
    @Json(name = "imdb_id")
    var imdbId: String,
    @Json(name = "original_language")
    var originalLanguage: String,
    @Json(name = "original_title")
    var originalTitle: String,
    @Json(name = "overview")
    var overview: String,
    @Json(name = "popularity")
    var popularity: Double,
    @Json(name = "poster_path")
    var posterPath: String,
    @Json(name = "release_date")
    var releaseDate: String,
    @Json(name = "revenue")
    var revenue: Int,
    @Json(name = "runtime")
    var runtime: Int,
//    @Json(name = "spoken_languages")
//    var spokenLanguages: List<SpokenLanguage>,
    @Json(name = "status")
    var status: String,
    @Json(name = "tagline")
    var tagline: String,
    @Json(name = "title")
    var title: String,
    @Json(name = "video")
    var video: Boolean,
    @Json(name = "videos")
    var videos: Videos,
    @Json(name = "vote_average")
    var voteAverage: Double,
    @Json(name = "vote_count")
    var voteCount: Int
)

@JsonClass(generateAdapter = true)
data class Genre(
    @Json(name = "id")
    var id: Int,
    @Json(name = "name")
    var name: String
)


//@JsonClass(generateAdapter = true)
//data class SpokenLanguage(
//    @Json(name = "english_name")
//    var englishName: String,
//    @Json(name = "iso_639_1")
//    var iso6391: String,
//    @Json(name = "name")
//    var name: String
//)