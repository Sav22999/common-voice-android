package org.commonvoice.saverio_lib.utils

import android.media.AudioAttributes
import android.os.Build
import androidx.annotation.RequiresApi

object AudioConstants {

    val AudioAttribute: AudioAttributes by lazy {
        AudioAttributes.Builder().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
            setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            setUsage(AudioAttributes.USAGE_MEDIA)
        }.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val VolumeControlStream = AudioAttribute.volumeControlStream
}