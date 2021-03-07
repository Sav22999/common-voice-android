package org.commonvoice.saverio.utils

import org.commonvoice.saverio_lib.api.responseBodies.ResponseLanguage
import org.commonvoice.saverio_lib.repositories.LanguagesRepository
import java.text.SimpleDateFormat

class TranslationHandler(
    private val languagesRepository: LanguagesRepository
) {

    private val _availableLanguages = mutableListOf<TranslationEntry>()

    val availableLanguages: List<TranslationEntry>
        get() = _availableLanguages
    val notCompletelyTranslatedLanguages: List<TranslationEntry>
        get() = _availableLanguages.filter { it.languagePercentage < 90 }
    val availableLanguageNames: List<String>
        get() = _availableLanguages.map { it.languageName }
    val availableLanguageCodes: List<String>
        get() = _availableLanguages.map { it.languageCode }

    init {
        _availableLanguages.clear()
        _availableLanguages.addAll(convertResponseToEntryList(languagesRepository.localLanguages))
    }

    fun isLanguageSupported(languageCode: String): Boolean {
        return availableLanguages.count { it.languageCode == languageCode } > 0
    }

    fun isLanguageComplete(languageCode: String): Boolean {
        return (availableLanguages.find { it.languageCode == languageCode }?.languagePercentage ?: 0) >= 90
    }

    fun getLanguageName(languageCode: String): String {
        return (availableLanguages.find { it.languageCode == languageCode }?.languageName) ?: "English"
    }

    fun getLanguageCode(languageName: String): String {
        return (availableLanguages.find { it.languageName == languageName }?.languageCode) ?: "en"
    }

    suspend fun updateLanguages() {
        val serverLanguage = languagesRepository.getServerAvailableLanguages()
        val localLanguage = languagesRepository.localLanguages
        serverLanguage?.let { server ->
            if (dateMillis(server.lastUpdate) > dateMillis(localLanguage.lastUpdate)) {
                languagesRepository.localLanguages = server
            }
        }
        _availableLanguages.clear()
        _availableLanguages.addAll(convertResponseToEntryList(languagesRepository.localLanguages))
    }

    private fun convertResponseToEntryList(responseLanguage: ResponseLanguage): List<TranslationEntry> {
        return responseLanguage.languages
            .map { (key, value) ->
                TranslationEntry(
                    value.nativeName,
                    key,
                    value.percentageTranslated
                )
            }
    }

    data class TranslationEntry(

        val languageName: String,

        val languageCode: String,

        val languagePercentage: Int,

    )

    companion object {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        private fun dateMillis(date: String): Long = try {
            dateFormat.parse(date)?.time
        } catch (e: Exception) {
            null
        } ?: 0L

        const val DEFAULT_LANGUAGE = "en"

    }

}