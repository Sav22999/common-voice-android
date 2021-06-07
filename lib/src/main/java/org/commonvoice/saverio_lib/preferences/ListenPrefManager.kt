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
        set(value) = preferences.edit().putBoolean(Keys.SHOW_SPEED_CONTROL_LISTEN.name, value)
            .apply()

    var audioSpeed: Float
        get() = preferences.getFloat(Keys.AUDIO_SPEED_LISTEN.name, 1F)
        set(value) = preferences.edit().putFloat(Keys.AUDIO_SPEED_LISTEN.name, value).apply()

    /*
    value of gestures:
    "": nothing/disabled/not set
    "back": go back to the previous activity,
    "report": [Speak and Listen] report the current sentence/clip,
    "skip": [Speak and Listen] skip the current sentence/clip,
    "validate-yes": [Listen] accept the clip,
    "validate-no": [Listen] reject the clip,
    "info": [Speak and Listen] show the information about the current clip/sentence,
    "animations" enable/disable animations,
    "speed-control": [Speak and Listen] enable/disable the speed control bar,
    "auto-play": [Listen] enable/disable the auto-play,
    "save-recordings": [Speak] enable/disable the saving on file of the recordings,
    "skip-confirmation": [Speak] enable/disable the feature "skip recording confirmation",
    "indicator-sound": [Speak] enable/disable recording indicator sound
     */

    var gesturesSwipeTop: String
        get() = preferences.getString(Keys.GESTURES_SWIPE_TOP.name, "report") ?: ""
        set(value) = preferences.edit().putString(Keys.GESTURES_SWIPE_TOP.name, value).apply()

    var gesturesSwipeBottom: String
        get() = preferences.getString(Keys.GESTURES_SWIPE_BOTTOM.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.GESTURES_SWIPE_BOTTOM.name, value)
            .apply()

    var gesturesSwipeLeft: String
        get() = preferences.getString(Keys.GESTURES_SWIPE_LEFT.name, "skip") ?: ""
        set(value) = preferences.edit().putString(Keys.GESTURES_SWIPE_LEFT.name, value)
            .apply()

    var gesturesSwipeRight: String
        get() = preferences.getString(Keys.GESTURES_SWIPE_RIGHT.name, "back") ?: ""
        set(value) = preferences.edit().putString(Keys.GESTURES_SWIPE_RIGHT.name, value)
            .apply()

    var gesturesLongPress: String
        get() = preferences.getString(Keys.GESTURES_LONG_PRESS.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.GESTURES_LONG_PRESS.name, value)
            .apply()

    var gesturesDoubleTap: String
        get() = preferences.getString(Keys.GESTURES_DOUBLE_TAP.name, "") ?: ""
        set(value) = preferences.edit().putString(Keys.GESTURES_DOUBLE_TAP.name, value)
            .apply()

    private enum class Keys {
        REQUIRED_CLIPS_COUNT,
        AUTO_PLAY_CLIPS,
        SHOW_SENTENCE_AT_THE_END,
        SHOW_AD_BANNER,
        SHOW_SPEED_CONTROL_LISTEN,
        AUDIO_SPEED_LISTEN,

        NO_MORE_CLIPS_AVAILABLE,

        GESTURES_SWIPE_TOP,
        GESTURES_SWIPE_BOTTOM,
        GESTURES_SWIPE_LEFT,
        GESTURES_SWIPE_RIGHT,
        GESTURES_LONG_PRESS,
        GESTURES_DOUBLE_TAP,
    }

}