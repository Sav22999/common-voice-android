package org.commonvoice.saverio_lib.mediaPlayer

import android.content.Context
import android.media.MediaPlayer
import org.commonvoice.saverio_lib.models.Recording

class MediaPlayerRepository(private val ctx: Context) {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()

    fun setup(callback: () -> Unit) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnCompletionListener {
            callback()
        }
    }

    fun reset() {
        mediaPlayer?.reset()
    }

    fun playRecording(recording: Recording) {
        mediaPlayer?.apply {
            setDataSource(ByteArrayDataSource(recording))
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
        mediaPlayer?.release()
        mediaPlayer = null
    }

}