package org.commonvoice.saverio_lib.preferences

import android.content.Context

class FirstRunPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("firstRunPreferences", Context.MODE_PRIVATE)

    var speak: Boolean
        get() = preferences.getBoolean(Keys.SPEAK.name, true)
        set(value) {
            preferences.edit().putBoolean(Keys.SPEAK.name, value).apply()
        }

    private enum class Keys {
        SPEAK
    }

}