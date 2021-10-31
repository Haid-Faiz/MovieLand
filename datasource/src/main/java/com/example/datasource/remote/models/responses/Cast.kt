package com.example.datasource.remote.models.responses


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cast(
    @Json(name = "adult")
    var adult: Boolean,
    @Json(name = "cast_id")
    var castId: Int?,
    @Json(name = "character")
    var character: String,
    @Json(name = "credit_id")
    var creditId: String,
    @Json(name = "gender")
    var gender: Int,
    @Json(name = "id")
    var id: Int,
    @Json(name = "known_for_department")
    var knownForDepartment: String?,
    @Json(name = "name")
    var name: String,
    @Json(name = "order")
    var order: Int,
    @Json(name = "original_name")
    var originalName: String,
    @Json(name = "popularity")
    var popularity: Double,
    @Json(name = "profile_path")
    var profilePath: String?
)