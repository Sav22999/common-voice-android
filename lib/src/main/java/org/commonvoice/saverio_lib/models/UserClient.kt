package org.commonvoice.saverio_lib.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserClient(

    @Json(name = "age")
    val age: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "gender")
    val gender: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "avatar_url")
    val avatar_url: String?,

    @Json(name = "avatar_clip_url")
    val avatar_clip_url: String?,

    @Json(name = "clips_count")
    val clips_count: Int,

    @Json(name = "votes_count")
    val votes_count: Int

)