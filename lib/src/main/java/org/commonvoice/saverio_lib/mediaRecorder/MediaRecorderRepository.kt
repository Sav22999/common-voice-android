package org.commonvoice.saverio_lib.mediaRecorder

import android.media.MediaRecorder
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.models.Sentence

class MediaRecorderRepository(
    private val fileHolder: FileHolder
) {

    private var recorder: MediaRecorder? = null

    fun setupRecorder() {
        recorder?.release()
        recorder = null
        fileHolder.reset()
        recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setMaxDuration(20000)
                setAudioEncodingBitRate(16 * 44100)
                setAudioSamplingRate(44100)
                setOutputFile(fileHolder.fileDescriptor)
                prepare()
            }
    }

    fun startRecording() {
        setupRecorder()
        recorder!!.start()
    }

    fun stopRecordingAndReadData(sentence: Sentence): Recording {
        recorder!!.stop()
        val array = fileHolder.getByteArray()
        setupRecorder()
        return sentence.toRecording(array)
    }

    fun redoRecording() {
        setupRecorder()
        startRecording()
    }

    fun stop() {
        recorder?.stop()
    }

    fun clean() {
        recorder?.release()
        recorder = null
        fileHolder.clear()
    }

}