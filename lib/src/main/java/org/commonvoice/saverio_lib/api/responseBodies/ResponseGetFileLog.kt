package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseGetFileLog(
    @Json(name = "general")
    val generalInfo: GeneralInfo,

    @Json(name = "log")
    val logInfo: LogInfo
)

@JsonClass(generateAdapter = true)
data class GeneralInfo(
    @Json(name = "id")
    val id: Int,

    @Json(name = "date")
    val date: String,

    @Json(name = "language")
    val language: String,

    @Json(name = "version")
    val appVersion: String,

    @Json(name = "source")
    val appSource: String,

    @Json(name = "logged")
    val isLogged: Int,
)

@JsonClass(generateAdapter = true)
data class LogInfo(
    @Json(name = "errorLevel")
    val errorLevel: String,

    @Json(name = "tag")
    val tag: String,

    @Json(name = "stackTrace")
    val stackTrace: String,

    @Json(name = "additionalLogs")
    val additionalLogs: String,
)

@JsonClass(generateAdapter = true)
data class ResponsePostFileLog(
    @Json(name = "code")
    val resultCode: Int,

    @Json(name = "status")
    val resultStatus: String,

    @Json(name = "description")
    val resultDescription: String,
)