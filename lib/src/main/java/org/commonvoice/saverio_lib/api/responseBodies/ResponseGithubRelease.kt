package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseGithubRelease(

    @Json(name = "tag_name")
    val latestVersion: String

)