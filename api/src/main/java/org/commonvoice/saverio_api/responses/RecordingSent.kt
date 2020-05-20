package org.commonvoice.saverio_api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecordingSent(

    @Json(name = "filePrefix")
    val prefix: String

)