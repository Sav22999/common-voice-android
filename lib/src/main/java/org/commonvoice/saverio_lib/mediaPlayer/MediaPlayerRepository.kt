package org.commonvoice.saverio_lib.mediaPlayer

import android.content.Context
import android.media.MediaPlayer
import org.commonvoice.saverio_lib.R
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

    fun playClip(clip: Clip): Boolean {
        mediaPlayer?.apply {
            setDataSource(ByteArrayDataSource(clip))
            try {
                prepare()
            } catch (e: Exception) {
                //TODO
                println("Exception: " + e.toString())
                return false
            }
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