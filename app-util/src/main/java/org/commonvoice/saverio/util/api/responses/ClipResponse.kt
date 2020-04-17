package org.commonvoice.saverio.util.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClipResponse(

    @Json(name = "filePrefix")
    val filePrefix: String?

)