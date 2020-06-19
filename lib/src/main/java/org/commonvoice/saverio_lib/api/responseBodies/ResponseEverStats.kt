package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseEverStats(

    @Json(name = "total")
    val total: Int,

    @Json(name = "valid")
    val valid: Int

)