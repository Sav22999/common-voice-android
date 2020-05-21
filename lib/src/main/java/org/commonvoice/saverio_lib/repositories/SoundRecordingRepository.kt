package org.commonvoice.saverio_lib.repositories

import android.media.MediaRecorder
import org.commonvoice.saverio_lib.models.RecordableSentence

class SoundRecordingRepository() {

    private var recorder: MediaRecorder? = MediaRecorder()

    init {
        setupRecorder()
    }

    private fun setupRecorder() {
        if (recorder == null) {
            recorder = MediaRecorder()
        }
        recorder!!.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setMaxDuration(10001)
            setAudioEncodingBitRate(16 * 44100)
            setAudioSamplingRate(44100)
        }
    }

    fun startRecording(recordableSentence: RecordableSentence) {
        recorder?.apply {
            reset()
            setupRecorder()
            setOutputFile(recordableSentence.file.outputStream().fd)
            prepare()
            start()
        }
    }

    fun stopRecording() {
        recorder?.stop()
    }

    fun redoRecording(recordableSentence: RecordableSentence) {
        recordableSentence.resetFile()
        startRecording(recordableSentence)
    }

    fun clean() {
        recorder?.release()
        recorder = null
    }

}