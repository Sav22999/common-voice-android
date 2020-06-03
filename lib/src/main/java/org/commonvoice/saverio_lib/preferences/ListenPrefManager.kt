package org.commonvoice.saverio_lib.preferences

import android.content.Context

class ListenPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("listenPreferences", Context.MODE_PRIVATE)

    var requiredClipsCount: Int
        get() = preferences.getInt(Keys.REQUIRED_CLIPS_COUNT.name, 50)
        set(value) {
            preferences.edit().putInt(Keys.REQUIRED_CLIPS_COUNT.name, value).apply()
        }

    private enum class Keys {
        REQUIRED_CLIPS_COUNT
    }

}