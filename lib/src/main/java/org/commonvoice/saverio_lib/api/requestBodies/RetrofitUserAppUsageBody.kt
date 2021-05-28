package org.commonvoice.saverio_lib.api.requestBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RetrofitUserAppUsageBody(

    @Json(name = "logged")
    val logged: Int,

    @Json(name = "language")
    val language: String,

    @Json(name = "version")
    val version: Int,

    @Json(name = "source")
    val source: String,

    @Json(name = "type")
    val type: Int,

    @Json(name = "username")
    val username: String,

    @Json(name = "offline")
    val offline: Int,

    @Json(name = "sentence_id")
    val sentence_id: String,

    @Json(name = "clip_id")
    val clip_id: String,

    @Json(name = "details")
    val details: String,

    )