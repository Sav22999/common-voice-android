package org.commonvoice.saverio

class TranslationsLanguages {
    private var translations_languages: Array<String>? = null
    private var not_completed_translated: Array<String>? = null

    constructor() {
        // Change manually
        this.translations_languages =
            arrayOf(
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
        this.not_completed_translated =
            arrayOf(
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
    }

    fun getAll(): Array<String>? {
        return this.translations_languages
    }

    fun isSupported(lang: String): Boolean {
        if (this.translations_languages?.indexOf(lang) != -1) return true
        return false
    }

    fun isUncompleted(lang: String): Boolean {
        if (this.not_completed_translated?.indexOf(lang) != -1 || this.translations_languages?.indexOf(
                lang
            ) == -1
        ) return true
        return false
    }

    fun getDefaultLanguage(): String {
        return "en"
    }
}