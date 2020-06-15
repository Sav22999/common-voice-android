package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseLeaderboardPosition(

    @Json(name = "username")
    val username: String,

    @Json(name = "total")
    val total: Int,

    @Json(name = "you")
    val isYou: Boolean

)