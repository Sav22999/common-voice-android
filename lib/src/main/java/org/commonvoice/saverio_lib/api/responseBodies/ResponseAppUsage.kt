package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseAppUsage(

    @Json(name = "listen")
    val listen: Listen,

    @Json(name = "speak")
    val speak: Speak,

    ) {

    @JsonClass(generateAdapter = true)
    data class Listen(

        @Json(name = "validated")
        val validated: Int,

        @Json(name = "accepted")
        val accepted: Int,

        @Json(name = "rejected")
        val rejected: Int,

        @Json(name = "reported")
        val reported: Int,

        )

    @JsonClass(generateAdapter = true)
    data class Speak(

        @Json(name = "sent")
        val sent: Int,

        @Json(name = "reported")
        val reported: Int,

        )

}
