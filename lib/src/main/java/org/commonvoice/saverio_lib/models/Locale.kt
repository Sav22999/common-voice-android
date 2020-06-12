package org.commonvoice.saverio_lib.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Locale(

    @Json(name = "accent")
    val accent: String,

    @Json(name = "locale")
    val locale: String

)