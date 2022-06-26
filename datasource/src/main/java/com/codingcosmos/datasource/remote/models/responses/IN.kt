package com.codingcosmos.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IN(
    @Json(name = "link")
    var link: String? = null
//    @Json(name = "buy")
//    var buy: List<Provider>? = null,
//    @Json(name = "flatrate")
//    var flatrate: List<Provider>? = null,
//    @Json(name = "rent")
//    var rent: List<Provider>? = null
)
