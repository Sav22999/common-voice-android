package org.commonvoice.saverio_lib.api.responseBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseLanguage(

    @Json(name = "last-update")
    val lastUpdate: String,

    @Json(name = "languages")
    val languages: Map<String, Language>

) {

    @JsonClass(generateAdapter = true)
    data class Language(

        @Json(name = "english")
        val englishName: String,

        @Json(name = "native")
        val nativeName: String,

        @Json(name = "percentage")
        val percentageTranslated: Int,

        @Json(name = "crowdin")
        val onCrowdin: Boolean

    )

    companion object {

        const val DEFAULT_VALUE =
            "{" +
                "\"last-update\": \"0000-00-00 00:00:00\"," +
                "\"languages\": {" +
                    "\"en\": {" +
                        "\"english\": \"English\"," +
                        "\"native\": \"English\"," +
                        "\"percentage\": 100," +
                        "\"crowdin\": true" +
                    "}" +
                "}" +
            "}"

        val DEFAULT_INSTANCE = ResponseLanguage(
            "0000-00-00 00:00:00",
            mapOf(
                "en" to Language("English", "English", 100, true)
            )
        )

    }

}