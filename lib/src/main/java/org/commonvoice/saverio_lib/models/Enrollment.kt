package org.commonvoice.saverio_lib.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Enrollment(

    @Json(name = "team")
    val team: String?,

    @Json(name = "challenge")
    val challenge: String?,

    @Json(name = "invite")
    val invite: String?

)