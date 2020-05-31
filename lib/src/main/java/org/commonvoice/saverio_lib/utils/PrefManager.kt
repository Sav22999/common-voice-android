package org.commonvoice.saverio_lib.utils

import android.content.Context

class PrefManager(private val ctx: Context) {

    private val preferences = ctx.getSharedPreferences("mainPreferences", Context.MODE_PRIVATE)

    var language: String
        get() = preferences.getString(Keys.LANGUAGE.name, "en") ?: "en"
        set(value) {
            preferences.edit().putString(Keys.LANGUAGE.name, value).apply()
        }

    var tokenUserId: String
        get() = preferences.getString(Keys.TOKEN_USERID.name, "") ?: ""
        set(value) {
            preferences.edit().putString(Keys.TOKEN_USERID.name, value).apply()
        }

    var tokenAuth: String
        get() = preferences.getString(Keys.TOKEN_AUTH.name, "") ?: ""
        set(value) {
            preferences.edit().putString(Keys.TOKEN_AUTH.name, value).apply()
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

    var areGesturesEnabled: Boolean
        get() = preferences.getBoolean(Keys.GESTURES_ENABLED.name, false)
        set(value) {
            preferences.edit().putBoolean(Keys.GESTURES_ENABLED.name, value).apply()
        }

    val deviceOrientation: Int
        get() = ctx.resources.configuration.orientation

    private enum class Keys {
        LANGUAGE,
        SESSID_COOKIE,
        TOKEN_USERID,
        TOKEN_AUTH,
        REQUIRED_SENTENCES_COUNT,
        PERIODICALLY_REFRESH_SENTENCES,
        GESTURES_ENABLED
    }

}