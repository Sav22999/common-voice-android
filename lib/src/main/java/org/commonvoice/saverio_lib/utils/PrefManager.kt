package org.commonvoice.saverio_lib.utils

import android.content.Context

class PrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("mainPreferences", Context.MODE_PRIVATE)

    var language: String
        get() = preferences.getString(Keys.LANGUAGE.name, "en") ?: "en"
        set(value) {
            preferences.edit().putString(Keys.LANGUAGE.name, value).apply()
        }

    var sessIdCookie: String?
        get() = preferences.getString(Keys.SESSID_COOKIE.name, null)
        set(value) {
            preferences.edit().putString(Keys.SESSID_COOKIE.name, value).apply()
        }

    var requiredSentencesCount: Int
        get() = preferences.getInt(Keys.REQUIRED_SENTENCES_COUNT.name, 50)
        set(value) {
            preferences.edit().putInt(Keys.REQUIRED_SENTENCES_COUNT.name, value).apply()
        }

    var periodicallyRefreshSentences: Boolean
        get() = preferences.getBoolean(Keys.PERIODICALLY_REFRESH_SENTENCES.name, false)
        set(value) {
            preferences.edit().putBoolean(Keys.PERIODICALLY_REFRESH_SENTENCES.name, value).apply()
        }

    private enum class Keys {
        LANGUAGE,
        SESSID_COOKIE,
        REQUIRED_SENTENCES_COUNT,
        PERIODICALLY_REFRESH_SENTENCES,
    }

}