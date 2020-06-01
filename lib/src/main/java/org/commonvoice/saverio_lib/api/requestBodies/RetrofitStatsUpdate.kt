package org.commonvoice.saverio_lib.api.requestBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RetrofitStatsUpdate(

    @Json(name = "username")
    val username: String,

    @Json(name = "logged")
    val isLogged: Int,

    @Json(name = "language")
    val language: String,

    @Json(name = "version")
    val appVersion: String,

    @Json(name = "public")
    val isPublic: Boolean,

    @Json(name = "source")
    val appSource: String

)