package org.commonvoice.saverio_lib.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(

    @Json(name = "id")
    val id: Int,

    @Json(name = "type")
    val type: Int?,

    @Json(name = "user")
    val userFilter: String?,

    @Json(name = "versionCode")
    val versionCodeFilter: String?,

    @Json(name = "language")
    val languageFilter: String?,

    @Json(name = "source")
    val sourceFilter: String?,

    @Json(name = "startDate")
    val startDateFilter: String?,

    @Json(name = "endDate")
    val endDateFilter: String?,

    @Json(name = "text")
    val text: String,

    @Json(name = "button1")
    val button1Text: String?,

    @Json(name = "button2")
    val button2Text: String?,

    @Json(name = "button1Link")
    val button1Link: String?,

    @Json(name = "button2Link")
    val button2Link: String?,

    @Json(name = "ableToClose")
    val canBeClosed: Boolean?,

    )