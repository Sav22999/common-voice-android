package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseVoicesToday(

    @Json(name = "date")
    val date: String,

    @Json(name = "value")
    val count: Int

)