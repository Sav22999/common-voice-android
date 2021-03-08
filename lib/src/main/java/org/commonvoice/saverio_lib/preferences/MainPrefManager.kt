package org.commonvoice.saverio_lib.preferences

import android.content.Context

class MainPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("mainPreferences", Context.MODE_PRIVATE)

    var language: String
        get() = preferences.getString(Keys.LANGUAGE.name, "en") ?: "en"
        set(value) = preferences.edit().putString(Keys.LANGUAGE.name, value).apply()

    var tokenUserId: String
        get() = preferences.getString(Keys.TOKEN_USERID.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.TOKEN_USERID.name, value).apply()

    var tokenAuth: String
        get() = preferences.getString(Keys.TOKEN_AUTH.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.TOKEN_AUTH.name, value).apply()

    var username: String
        get() = preferences.getString(Keys.USERNAME.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.USERNAME.name, value).apply()

    var isLoggedIn: Boolean
        get() = preferences.getBoolean(Keys.IS_LOGGED_IN.name, false)
        set(value) = preferences.edit().putBoolean(Keys.IS_LOGGED_IN.name, value).apply()

    var sessIdCookie: String?
        get() = preferences.getString(Keys.SESSID_COOKIE.name, null)
        set(value) = preferences.edit().putString(Keys.SESSID_COOKIE.name, value).apply()

    var showOfflineModeMessage: Boolean
        get() = preferences.getBoolean(Keys.SHOW_OFFLINE_MODE_MESSAGE.name, true)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_OFFLINE_MODE_MESSAGE.name, value)
            .apply()

    var showReportWebsiteBugs: Boolean
        get() = preferences.getBoolean(Keys.SHOW_REPORT_WEBSITE_BUGS.name, true)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_REPORT_WEBSITE_BUGS.name, value)
            .apply()

    var areGesturesEnabled: Boolean
        get() = preferences.getBoolean(Keys.GESTURES_ENABLED.name, true)
        set(value) = preferences.edit().putBoolean(Keys.GESTURES_ENABLED.name, value).apply()

    var statsUserId: String
        get() = preferences.getString(Keys.STATS_USERID.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.STATS_USERID.name, value).apply()

    var areGenericStats: Boolean
        get() = preferences.getBoolean(Keys.ARE_GENERIC_STATS.name, true)
        set(value) = preferences.edit().putBoolean(Keys.ARE_GENERIC_STATS.name, value).apply()

    var areAppUsageStatsEnabled: Boolean
        get() = preferences.getBoolean(Keys.ARE_APP_USAGE_STATS.name, true)
        set(value) = preferences.edit().putBoolean(Keys.ARE_APP_USAGE_STATS.name, value).apply()

    var areAnimationsEnabled: Boolean
        get() = preferences.getBoolean(Keys.ARE_ANIMATIONS_ENABLED.name, true)
        set(value) = preferences.edit().putBoolean(Keys.ARE_ANIMATIONS_ENABLED.name, value).apply()

    var areLabelsBelowMenuIcons: Boolean
        get() = preferences.getBoolean(Keys.LABELS_MENU_ICONS.name, false)
        set(value) = preferences.edit().putBoolean(Keys.LABELS_MENU_ICONS.name, value).apply()

    var hasLanguageChanged: Boolean
        get() = preferences.getBoolean(Keys.LANGUAGE_CHANGED.name, true)
        set(value) = preferences.edit().putBoolean(Keys.LANGUAGE_CHANGED.name, value).apply()

    var hasLanguageChanged2: Boolean
        get() = preferences.getBoolean(Keys.LANGUAGE_CHANGED2.name, true)
        set(value) = preferences.edit().putBoolean(Keys.LANGUAGE_CHANGED2.name, value).apply()

    var themeType: String?
        get() = preferences.getString(Keys.THEME_TYPE.name, "light") //{"light"|"dark"|"auto"}
        set(value) = preferences.edit().putString(Keys.THEME_TYPE.name, value).apply()

    var textSize: Float
        get() = preferences.getFloat(Keys.TEXT_SIZE.name, 1.0F)
        set(value) = preferences.edit().putFloat(Keys.TEXT_SIZE.name, value).apply()

    var appVersionCode: Int
        get() = preferences.getInt(Keys.APP_VERSION_CODE.name, 0)
        set(value) = preferences.edit().putInt(Keys.APP_VERSION_CODE.name, value).apply()

    var appSourceStore: String
        get() = preferences.getString(Keys.APP_SOURCE_STORE.name, null) ?: ""
        set(value) = preferences.edit().putString(Keys.APP_SOURCE_STORE.name, value).apply()

    var isAlpha: Boolean
        get() = preferences.getBoolean(Keys.IS_ALPHA.name, false)
        set(value) = preferences.edit().putBoolean(Keys.IS_ALPHA.name, value).apply()

    var isBeta: Boolean
        get() = preferences.getBoolean(Keys.IS_BETA.name, false)
        set(value) = preferences.edit().putBoolean(Keys.IS_BETA.name, value).apply()

    var showAdBanner: Boolean
        get() = preferences.getBoolean(Keys.SHOW_AD_BANNER.name, true)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_AD_BANNER.name, value).apply()

    var shownMessagesId: List<Int>
        get() = preferences.getString(Keys.SHOWN_MESSAGES_ID.name, "")?.split(',')
            ?.map { it.toIntOrNull() ?: -1 } ?: emptyList()
        set(value) = preferences.edit()
            .putString(Keys.SHOWN_MESSAGES_ID.name, value.joinToString(",")).apply()


    private enum class Keys {
        LANGUAGE,
        SESSID_COOKIE,
        TOKEN_USERID,
        TOKEN_AUTH,
        IS_LOGGED_IN,
        GESTURES_ENABLED,
        STATS_USERID,
        ARE_GENERIC_STATS,
        ARE_APP_USAGE_STATS,
        ARE_ANIMATIONS_ENABLED,
        LABELS_MENU_ICONS,
        SHOW_OFFLINE_MODE_MESSAGE,
        SHOW_REPORT_WEBSITE_BUGS,
        LANGUAGE_CHANGED,
        LANGUAGE_CHANGED2,
        THEME_TYPE,
        USERNAME,
        APP_VERSION_CODE,
        APP_SOURCE_STORE,
        IS_ALPHA,
        IS_BETA,
        SHOW_AD_BANNER,
        TEXT_SIZE,
        SHOWN_MESSAGES_ID,
    }

}