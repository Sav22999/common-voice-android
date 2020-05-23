package org.commonvoice.saverio

class TranslationsLanguages {
    private var translations_languages: Array<String>? = null
    private var not_completed_translated: Array<String>? = null

    constructor() {
        // Change manually
        this.translations_languages =
            arrayOf(
                "ar",
                "bn",
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
                "nl",
                "pl",
                "ru",
                "sv",
                "ta",
                "tr"
            )
        this.not_completed_translated =
            arrayOf(
                "bn",
                "ca",
                "cs",
                "de",
                "eo",
                "es",
                "et",
                "fa",
                "nl",
                "ru",
                "sv",
                "tr"
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