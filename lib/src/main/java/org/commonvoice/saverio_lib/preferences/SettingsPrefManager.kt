package org.commonvoice.saverio_lib.preferences

import android.content.Context

class SettingsPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("settingsPreferences", Context.MODE_PRIVATE)

    var isOfflineMode: Boolean
        get() = preferences.getBoolean(Keys.OFFLINE_MODE.name, true)
        set(value) = preferences.edit().putBoolean(Keys.OFFLINE_MODE.name, value).apply()

    var automaticallyCheckForUpdates: Boolean
        get() = preferences.getBoolean(Keys.AUTOMATICALLY_CHECK_FOR_UPDATES.name, true)
        set(value) = preferences.edit().putBoolean(Keys.AUTOMATICALLY_CHECK_FOR_UPDATES.name, value)
            .apply()

    var showReportIcon: Boolean
        get() = preferences.getBoolean(Keys.SHOW_REPORT_ICON.name, false)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_REPORT_ICON.name, value)
            .apply()

    var latestVersion: String
        get() = preferences.getString(Keys.LATEST_VERSION.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.LATEST_VERSION.name, value).apply()

    private enum class Keys {
        AUTOMATICALLY_CHECK_FOR_UPDATES,
        LATEST_VERSION,
        OFFLINE_MODE,
        SHOW_REPORT_ICON,
    }

}