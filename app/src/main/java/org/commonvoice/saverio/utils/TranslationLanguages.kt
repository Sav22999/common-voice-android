package org.commonvoice.saverio.utils

object TranslationLanguages {

    private val translation_languages: List<String> = listOf(
        "ar",
        "as",
        "ca",
        "cs",
        "de",
        "en",
        "eo",
        "es",
        "et",
        "eu",
        "fa",
        "fr",
        "ia",
        "it",
        "ja",
        "kab",
        "nl",
        "pl",
        "pt",
        "ro",
        "ru",
        "sv",
        "ta",
        "tr",
        "uk",
        "zh-CN"
    )

    private val not_completely_translated: List<String> = listOf(
        "as",
        "eu",
        "ca",
        "eo",
        "et",
        "fa",
        "ia",
        "ja",
        "pt",
        "ro",
        "ru",
        "sv",
        "tr",
        "uk",
        "zh-CN"
    )

    fun isSupported(lang: String): Boolean
        = translation_languages.contains(lang)

    fun isUncompleted(lang: String): Boolean {
        return not_completely_translated.contains(lang) || !isSupported(lang)
    }

    const val defaultLanguage: String = "en"

}