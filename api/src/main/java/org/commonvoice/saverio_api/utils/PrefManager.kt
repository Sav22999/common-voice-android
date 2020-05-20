package org.commonvoice.saverio_api.utils

import android.content.Context

class PrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("mainPreferences", Context.MODE_PRIVATE)

    var language: String
        get() = preferences.getString(Keys.LANGUAGE.name, "en") ?: "en"
        set(value) {
            preferences.edit().putString(Keys.LANGUAGE.name, value).apply()
        }

    var sessIdCookie: String?
        get() = preferences.getString(Keys.SESSIDCOOKIE.name, null)
        set(value) {
            preferences.edit().putString(Keys.SESSIDCOOKIE.name, value).apply()
        }

    private enum class Keys {
        LANGUAGE,
        SESSIDCOOKIE
    }

}