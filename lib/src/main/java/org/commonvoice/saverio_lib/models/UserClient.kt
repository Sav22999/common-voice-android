package org.commonvoice.saverio_lib.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserClient(

    @Json(name = "accent")
    val accent: String,

    @Json(name = "age")
    val age: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "gender")
    val gender: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "basket_token")
    val basket_token: String?,

    @Json(name = "skip_submission_feedback")
    val skip_submission_feedback: Int,

    @Json(name = "visible")
    val visible: Int,

    @Json(name = "avatar_url")
    val avatar_url: String?,

    @Json(name = "avatar_clip_url")
    val avatar_clip_url: String?,

    @Json(name = "clips_count")
    val clips_count: Int,

    @Json(name = "votes_count")
    val votes_count: Int,

    @Json(name = "locales")
    val locales: List<Locale>,

    @Json(name = "awards")
    val awards: List<String>,

    @Json(name = "custom_goals")
    val custom_goals: List<String>,

    @Json(name = "enrollment")
    val enrollment: Enrollment

)