package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseDailyUsage(

    @Json(name = "users")
    val users: Int,

    @Json(name = "logged")
    val logged: Int

)