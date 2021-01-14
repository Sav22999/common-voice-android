package org.commonvoice.saverio.utils

object TranslationLanguages {
    //TODO: Update manually this in every version

    //all languages supported in the app (AND also present on Crowdin!!!)
    private val translation_languages: List<String> = listOf(
        "ar",
        "as",
        "br",
        "ca",
        "cs",
        "cy",
        "de",
        "el",
        "en",
        "eo",
        "es",
        "et",
        "eu",
        "fa",
        "fi",
        "fr",
        "fy",
        "ia",
        "id",
        "it",
        "ja",
        "kab",
        "ky",
        "lg",
        "lt",
        "lv",
        "mn",
        "mt",
        "nl",
        "or",
        "pa",
        "pl",
        "pt",
        "ro",
        "ru",
        "sk",
        "sl",
        "sv",
        "ta",
        "tr",
        "uk",
        "zh-CN",
        "zh-HK",
        "zh-TW"
    )

    //all languages not completely translated (<90% translations)
    private val not_completely_translated: List<String> = listOf(
        "ar",
        "as",
        "br",
        "de",
        "el",
        "eo",
        "et",
        "eu",
        "fa",
        "fi",
        "fy",
        "id",
        "ja",
        "ky",
        "lg",
        "lv",
        "mn",
        "mt",
        "nl",
        "or",
        "pa",
        "ro",
        "ru",
        "sk",
        "sl",
        "sv",
        "ta",
        "tr",
        "uk",
        "zh-HK",
        "zh-TW"
    )

    fun isSupported(lang: String): Boolean = translation_languages.contains(lang)

    fun isUncompleted(lang: String): Boolean {
        return not_completely_translated.contains(lang) || !isSupported(lang)
    }

    const val defaultLanguage: String = "en"

}