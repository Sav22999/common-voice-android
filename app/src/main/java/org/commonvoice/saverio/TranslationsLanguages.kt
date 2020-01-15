package org.commonvoice.saverio

class TranslationsLanguages {
    private var translations_languages: Array<String>? = null

    constructor() {
        this.translations_languages = arrayOf("en", "eu", "fr", "ia", "it", "sv") //change manually
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