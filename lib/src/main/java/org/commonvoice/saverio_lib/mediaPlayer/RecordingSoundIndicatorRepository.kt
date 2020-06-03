package org.commonvoice.saverio_lib.mediaPlayer

import android.content.Context
import android.media.MediaPlayer
import org.commonvoice.saverio_lib.R

class RecordingSoundIndicatorRepository(private val ctx: Context) {

    private var startedSoundPlayer: MediaPlayer? = null
    private var finishedSoundPlayer: MediaPlayer? = null

    init {
        initStartedPlayer()
        initFinishedPlayer()
    }

    private fun initStartedPlayer() {
        if (startedSoundPlayer != null) {
            cleanStartedPlayer()
        }

        startedSoundPlayer = MediaPlayer.create(ctx, R.raw.started)
    }

    fun playStartedSound(onCompletion: () -> Unit) {
        if (startedSoundPlayer == null) {
            initStartedPlayer()
        }

        startedSoundPlayer!!.setOnCompletionListener {
            onCompletion()
        }

        startedSoundPlayer!!.start()
    }

    private fun cleanStartedPlayer() {
        startedSoundPlayer?.release()
        startedSoundPlayer = null
    }

    private fun initFinishedPlayer() {
        if (finishedSoundPlayer != null) {
            cleanFinishedPlayer()
        }

        finishedSoundPlayer = MediaPlayer.create(ctx, R.raw.finished)
    }

    fun playFinishedSound() {
        if (finishedSoundPlayer == null) {
            initFinishedPlayer()
        }

        finishedSoundPlayer!!.start()
    }

    private fun cleanFinishedPlayer() {
        finishedSoundPlayer?.release()
        finishedSoundPlayer = null
    }

    fun clean() {
        cleanStartedPlayer()
        cleanFinishedPlayer()
    }

}