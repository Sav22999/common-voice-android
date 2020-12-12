package org.commonvoice.saverio_lib.api.requestBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RetrofitFileLogUpdate(

    @Json(name = "logged")
    val isLogged: Int,

    @Json(name = "language")
    val language: String,

    @Json(name = "version")
    val appVersion: String,

    @Json(name = "source")
    val appSource: String,

    @Json(name = "errorLevel")
    val errorLevel: String,

    @Json(name = "tag")
    val tag: String,

    @Json(name = "stackTrace")
    val stackTrace: String,

    @Json(name = "additionalLogs")
    val additionalLogs: String
) {
    constructor(
        isLogged: Int,
        language: String,
        appVersion: String,
        appSource: String,
        stackTrace: String
    ) : this(
        isLogged = isLogged,
        language = language,
        appVersion = appVersion,
        appSource = appSource,
        errorLevel = "error",
        stackTrace = stackTrace,
        tag = "tag",
        additionalLogs = ""
    )
}