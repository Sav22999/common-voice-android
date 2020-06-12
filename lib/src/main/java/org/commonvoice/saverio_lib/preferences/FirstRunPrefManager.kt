package org.commonvoice.saverio_lib.preferences

import android.content.Context

class FirstRunPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("firstRunPreferences", Context.MODE_PRIVATE)

    var main: Boolean
        get() = preferences.getBoolean(Keys.MAIN.name, true)
        set(value) {
            preferences.edit().putBoolean(Keys.MAIN.name, value).apply()
        }

    var speak: Boolean
        get() = preferences.getBoolean(Keys.SPEAK.name, true)
        set(value) {
            preferences.edit().putBoolean(Keys.SPEAK.name, value).apply()
        }

    var listen: Boolean
        get() = preferences.getBoolean(Keys.LISTEN.name, true)
        set(value) {
            preferences.edit().putBoolean(Keys.LISTEN.name, value).apply()
        }

    private enum class Keys {
        MAIN,
        SPEAK,
        LISTEN,
    }

}