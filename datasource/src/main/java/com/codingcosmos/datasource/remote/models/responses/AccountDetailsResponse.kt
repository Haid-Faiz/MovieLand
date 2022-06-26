package com.codingcosmos.datasource.remote.models.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountDetailsResponse(
    @Json(name = "avatar")
    var avatar: Avatar,
    @Json(name = "id")
    var id: Int,
    @Json(name = "include_adult")
    var includeAdult: Boolean,
//    @Json(name = "iso_3166_1")
//    var iso31661: String,
//    @Json(name = "iso_639_1")
//    var iso6391: String,
    @Json(name = "name")
    var name: String,
    @Json(name = "username")
    var username: String
)

@JsonClass(generateAdapter = true)
data class Avatar(
    @Json(name = "gravatar")
    var gravatar: Gravatar,
    @Json(name = "tmdb")
    var tmdb: Tmdb
)

@JsonClass(generateAdapter = true)
data class Gravatar(
    @Json(name = "hash")
    var hash: String
)

@JsonClass(generateAdapter = true)
data class Tmdb(
    @Json(name = "avatar_path")
    var avatarPath: String?
)

// To use Gravatar image build url like:
// https://secure.gravatar.com/avatar/c9e9fc152ee756a900db85757c29815d.jpg?s=200

// If you wanted to use the TMDb avatar, you could build a URL like this:
// https://image.tmdb.org/t/p/w200/xy44UvpbTgzs9kWmp4C3fEaCl5h.png
