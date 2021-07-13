package org.commonvoice.saverio_lib.mediaPlayer

import android.media.MediaPlayer
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.utils.AudioConstants
import timber.log.Timber

class MediaPlayerRepository {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()

    fun setup(callback: () -> Unit) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                callback()
            }
        }
    }

    fun playRecording(recording: Recording, speed: Float = 1.0F) {
        mediaPlayer?.apply {
            setAudioAttributes(AudioConstants.AudioAttribute)
            setDataSource(ByteArrayDataSource(recording))
            prepare()
            playbackParams = playbackParams.setSpeed(speed)
            start()
        }
    }

    fun playClip(clip: Clip, speed: Float = 1.0F): Boolean {
        mediaPlayer?.apply {
            setAudioAttributes(AudioConstants.AudioAttribute)
            setDataSource(ByteArrayDataSource(clip))
            try {
                prepare()
            } catch (e: Exception) {
                //TODO
                Timber.e(e)
                return false
            }
            playbackParams = playbackParams.setSpeed(speed)
            start()
        }
        return true
    }

    fun stopPlaying() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
    }

    fun clean() {
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}