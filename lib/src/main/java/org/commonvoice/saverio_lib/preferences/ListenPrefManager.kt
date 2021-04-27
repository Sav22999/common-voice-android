package org.commonvoice.saverio_lib.preferences

import android.content.Context

class ListenPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("listenPreferences", Context.MODE_PRIVATE)

    var requiredClipsCount: Int
        get() = preferences.getInt(Keys.REQUIRED_CLIPS_COUNT.name, 50)
        set(value) = preferences.edit().putInt(Keys.REQUIRED_CLIPS_COUNT.name, value).apply()

    var isAutoPlayClipEnabled: Boolean
        get() = preferences.getBoolean(Keys.AUTO_PLAY_CLIPS.name, true)
        set(value) = preferences.edit().putBoolean(Keys.AUTO_PLAY_CLIPS.name, value).apply()

    var isShowTheSentenceAtTheEnd: Boolean
        get() = preferences.getBoolean(Keys.SHOW_SENTENCE_AT_THE_END.name, false)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_SENTENCE_AT_THE_END.name, value)
            .apply()

    var noMoreClipsAvailable: Boolean
        get() = preferences.getBoolean(Keys.NO_MORE_CLIPS_AVAILABLE.name, false)
        set(value) = preferences.edit().putBoolean(Keys.NO_MORE_CLIPS_AVAILABLE.name, value).apply()

    var showAdBanner: Boolean
        get() = preferences.getBoolean(Keys.SHOW_AD_BANNER.name, true)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_AD_BANNER.name, value).apply()

    var showSpeedControl: Boolean
        get() = preferences.getBoolean(Keys.SHOW_SPEED_CONTROL_LISTEN.name, false)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_SPEED_CONTROL_LISTEN.name, value).apply()

    var audioSpeed: Float
        get() = preferences.getFloat(Keys.AUDIO_SPEED_LISTEN.name, 1F)
        set(value) = preferences.edit().putFloat(Keys.AUDIO_SPEED_LISTEN.name, value).apply()

    private enum class Keys {
        REQUIRED_CLIPS_COUNT,
        AUTO_PLAY_CLIPS,
        SHOW_SENTENCE_AT_THE_END,
        SHOW_AD_BANNER,
        SHOW_SPEED_CONTROL_LISTEN,
        AUDIO_SPEED_LISTEN,

        NO_MORE_CLIPS_AVAILABLE,
    }

}