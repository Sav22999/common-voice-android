package org.commonvoice.saverio_lib.mediaPlayer

import android.media.MediaPlayer
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.models.Recording

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

    fun playRecording(recording: Recording) {
        mediaPlayer?.apply {
            setDataSource(ByteArrayDataSource(recording))
            prepare()
            start()
        }
    }

    fun playClip(clip: Clip) {
        mediaPlayer?.apply {
            setDataSource(ByteArrayDataSource(clip))
            prepare()
            start()
        }
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