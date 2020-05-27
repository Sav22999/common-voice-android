package org.commonvoice.saverio_lib.repositories

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
//import org.commonvoice.saverio_lib.models.RecordableSentence

class SoundListeningRepository(private val ctx: Context) {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()

    fun setup(callback: () -> Unit) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }
        mediaPlayer!!.setOnCompletionListener {
            callback()
        }
    }

    fun reset() {
        mediaPlayer?.reset()
    }

    fun playRecording(/*recording: RecordableSentence*/) {
        /*mediaPlayer?.apply {
            setDataSource(ctx, recording.file.toUri())
            prepare()
            start()
        }*/
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