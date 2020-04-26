package org.commonvoice.saverio

class TranslationsLanguages {
    private var translations_languages: Array<String>? = null

    constructor() {
        // Change manually
        this.translations_languages =
            arrayOf("cs", "de", "en", "eo", "es", "eu", "fr", "ia", "it", "nl", "ru", "sv", "tr")
    }

    fun getAll(): Array<String>? {
        return this.translations_languages
    }

    fun isSupported(lang: String): Boolean {
        if (this.translations_languages?.indexOf(lang) != -1) return true
        return false
    }

    fun getDefaultLanguage(): String {
        return "en"
    }
}