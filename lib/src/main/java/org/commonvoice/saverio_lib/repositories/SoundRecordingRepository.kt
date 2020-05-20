package org.commonvoice.saverio_lib.repositories

import android.media.MediaRecorder
import org.commonvoice.saverio_lib.models.Recording

class SoundRecordingRepository(private val recorder: MediaRecorder) {

    init {
        setupRecorder()
    }

    private fun setupRecorder() {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setMaxDuration(10001)
            setAudioEncodingBitRate(16 * 44100)
            setAudioSamplingRate(44100)
        }
    }

    fun startRecording(recording: Recording) {
        recorder.apply {
            reset()
            setupRecorder()
            setOutputFile(recording.file.outputStream().fd)
            prepare()
            start()
        }
    }

    fun stopRecording() {
        recorder.stop()
    }

    fun clean() {
        recorder.release()
    }

}