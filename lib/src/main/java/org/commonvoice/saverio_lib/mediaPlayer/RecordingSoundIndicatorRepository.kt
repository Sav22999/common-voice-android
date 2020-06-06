package org.commonvoice.saverio_lib.mediaPlayer

import android.content.Context
import android.media.MediaPlayer
import org.commonvoice.saverio_lib.R

class RecordingSoundIndicatorRepository(private val ctx: Context) {

    private var startingSoundPlayer: MediaPlayer? = null
    private var finishingSoundPlayer: MediaPlayer? = null

    init {
        initStartingPlayer()
        initFinishingPlayer()
    }

    private fun initStartingPlayer() {
        if (startingSoundPlayer != null) {
            cleanStartingPlayer()
        }

        startingSoundPlayer = MediaPlayer.create(ctx, R.raw.started)
    }

    fun playStartingSound(onCompletion: () -> Unit) {
        if (startingSoundPlayer == null) {
            initStartingPlayer()
        }

        startingSoundPlayer!!.setOnCompletionListener {
            onCompletion()
        }

        startingSoundPlayer!!.start()
    }

    private fun cleanStartingPlayer() {
        startingSoundPlayer?.release()
        startingSoundPlayer = null
    }

    private fun initFinishingPlayer() {
        if (finishingSoundPlayer != null) {
            cleanFinishingPlayer()
        }

        finishingSoundPlayer = MediaPlayer.create(ctx, R.raw.finished)
    }

    fun playFinishingSound() {
        if (finishingSoundPlayer == null) {
            initFinishingPlayer()
        }

        finishingSoundPlayer!!.start()
    }

    private fun cleanFinishingPlayer() {
        finishingSoundPlayer?.release()
        finishingSoundPlayer = null
    }

    fun clean() {
        cleanStartingPlayer()
        cleanFinishingPlayer()
    }

}