package org.commonvoice.saverio_lib.preferences

import android.content.Context

class SettingsPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("settingsPreferences", Context.MODE_PRIVATE)

    var showConfirmationMessages: Boolean
        get() = preferences.getBoolean(Keys.SHOW_CONFIRMATION.name, true)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_CONFIRMATION.name, value).apply()

    var enableExperimentalFeatures: Boolean
        get() = preferences.getBoolean(Keys.EXPERIMENTAL_FEATURES.name, false)
        set(value) = preferences.edit().putBoolean(Keys.EXPERIMENTAL_FEATURES.name, value).apply()

    var automaticallyCheckForUpdates: Boolean
        get() = preferences.getBoolean(Keys.AUTOMATICALLY_CHECK_FOR_UPDATES.name, true)
        set(value) = preferences.edit().putBoolean(Keys.AUTOMATICALLY_CHECK_FOR_UPDATES.name, value)
            .apply()

    var latestVersion: String
        get() = preferences.getString(Keys.LATEST_VERSION.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.LATEST_VERSION.name, value).apply()

    private enum class Keys {
        SHOW_CONFIRMATION,
        EXPERIMENTAL_FEATURES,
        AUTOMATICALLY_CHECK_FOR_UPDATES,
        LATEST_VERSION,
    }

}