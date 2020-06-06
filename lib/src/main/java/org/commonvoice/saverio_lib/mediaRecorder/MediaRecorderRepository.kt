package org.commonvoice.saverio_lib.mediaRecorder

import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.models.Sentence

class MediaRecorderRepository(
    private val fileHolder: FileHolder
) {

    private var recorder: MediaRecorder? = null

    private var recordingStartTimeStamp: Long = 0

    fun setupRecorder() {
        recorder?.release()
        recorder = null
        fileHolder.reset()
        recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setMaxDuration(10000)
                setAudioEncodingBitRate(16 * 44100)
                setAudioSamplingRate(44100)
                setOutputFile(fileHolder.fileDescriptor)
                prepare()
            }
    }

    fun startRecording() {
        setupRecorder()
        recorder!!.start()
        recordingStartTimeStamp = System.currentTimeMillis()
    }

    fun stopRecordingAndReadData(sentence: Sentence, onError: () -> Unit, onSuccess: (Recording) -> Unit) {
        try {
            when {
                System.currentTimeMillis() - recordingStartTimeStamp <= 500 -> {
                    onError()
                    return
                }
                System.currentTimeMillis() - recordingStartTimeStamp < 1000 -> {
                    recorder!!.stop()
                }
                else -> {
                    onError()
                    return
                }
            }
        } catch (e: IllegalStateException) {
            onError()
            return
        } catch (e: RuntimeException) {
            onError()
            return
        }
        val array = fileHolder.getByteArray()
        setupRecorder()
        onSuccess(sentence.toRecording(array))
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