package org.commonvoice.saverio_lib.preferences

import android.content.Context

class SpeakPrefManager(ctx: Context) {

    private val preferences = ctx.getSharedPreferences("speakPreferences", Context.MODE_PRIVATE)

    var requiredSentencesCount: Int
        get() = preferences.getInt(Keys.REQUIRED_SENTENCES_COUNT.name, 50)
        set(value) = preferences.edit().putInt(Keys.REQUIRED_SENTENCES_COUNT.name, value).apply()

    var periodicallyRefreshSentences: Boolean
        get() = preferences.getBoolean(Keys.PERIODICALLY_REFRESH_SENTENCES.name, false)
        set(value) = preferences.edit().putBoolean(Keys.PERIODICALLY_REFRESH_SENTENCES.name, value)
            .apply()

    var playRecordingSoundIndicator: Boolean
        get() = preferences.getBoolean(Keys.ENABLE_RECORDING_SOUND_INDICATOR.name, false)
        set(value) = preferences.edit()
            .putBoolean(Keys.ENABLE_RECORDING_SOUND_INDICATOR.name, value).apply()

    var skipRecordingConfirmation: Boolean
        get() = preferences.getBoolean(Keys.SKIP_RECORDING_CONFIRMATION.name, false)
        set(value) = preferences.edit().putBoolean(Keys.SKIP_RECORDING_CONFIRMATION.name, value)
            .apply()

    var saveRecordingsOnDevice: Boolean
        get() = preferences.getBoolean(Keys.SAVE_RECORDINGS_ON_DEVICE.name, false)
        set(value) = preferences.edit().putBoolean(Keys.SAVE_RECORDINGS_ON_DEVICE.name, value)
            .apply()

    var deviceRecordingsLocation: String
        get() = preferences.getString(Keys.DEVICE_RECORDINGS_LOCATION.name, null) ?: "/cv-project/"
        set(value) = preferences.edit().putString(Keys.DEVICE_RECORDINGS_LOCATION.name, value)
            .apply()

    var noMoreSentencesAvailable: Boolean
        get() = preferences.getBoolean(Keys.NO_MORE_SENTENCES_AVAILABLE.name, false)
        set(value) = preferences.edit().putBoolean(Keys.NO_MORE_SENTENCES_AVAILABLE.name, value)
            .apply()

    var showAdBanner: Boolean
        get() = preferences.getBoolean(Keys.SHOW_AD_BANNER.name, true)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_AD_BANNER.name, value).apply()

    var showSpeedControl: Boolean
        get() = preferences.getBoolean(Keys.SHOW_SPEED_CONTROL_SPEAK.name, false)
        set(value) = preferences.edit().putBoolean(Keys.SHOW_SPEED_CONTROL_SPEAK.name, value)
            .apply()

    var pushToTalk: Boolean
        get() = preferences.getBoolean(Keys.PUSH_TO_TALK.name, false)
        set(value) = preferences.edit().putBoolean(Keys.PUSH_TO_TALK.name, value)
            .apply()

    var audioSpeed: Float
        get() = preferences.getFloat(Keys.AUDIO_SPEED_SPEAK.name, 1F)
        set(value) = preferences.edit().putFloat(Keys.AUDIO_SPEED_SPEAK.name, value).apply()


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
    "play-stop-clip": [Listen] play/stop the clip
    "start-stop-recording": [Speak] start/stop the recording
    "play-stop-recording": [Speak] play/stop the recording
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
        REQUIRED_SENTENCES_COUNT,
        PERIODICALLY_REFRESH_SENTENCES,
        ENABLE_RECORDING_SOUND_INDICATOR,
        SKIP_RECORDING_CONFIRMATION,
        SAVE_RECORDINGS_ON_DEVICE,
        DEVICE_RECORDINGS_LOCATION,
        SHOW_AD_BANNER,
        SHOW_SPEED_CONTROL_SPEAK,
        AUDIO_SPEED_SPEAK,

        NO_MORE_SENTENCES_AVAILABLE,

        PUSH_TO_TALK,

        GESTURES_SWIPE_TOP,
        GESTURES_SWIPE_BOTTOM,
        GESTURES_SWIPE_LEFT,
        GESTURES_SWIPE_RIGHT,
        GESTURES_LONG_PRESS,
        GESTURES_DOUBLE_TAP,
    }

}