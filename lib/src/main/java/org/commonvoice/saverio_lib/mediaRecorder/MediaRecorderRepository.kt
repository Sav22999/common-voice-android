package org.commonvoice.saverio_lib.mediaRecorder

import android.media.MediaRecorder
import android.os.Build
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.models.Sentence
import timber.log.Timber

class MediaRecorderRepository(
    private val fileHolder: FileHolder
) {

    private var recorder: MediaRecorder? = null

    private var recordingStartTimeStamp: Long = 0

    private var suppressError: Boolean = false

    fun setupRecorder() {
        recorder?.reset()
        recorder?.release()
        recorder = null
        fileHolder.reset()
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                setAudioSamplingRate(48000)
            } else {
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(32000)
            }
            setMaxDuration(10000)
            setAudioEncodingBitRate(48 * 1024)
            setOutputFile(fileHolder.fileDescriptor)
            prepare()
        }
    }

    fun startRecording() {
        setupRecorder()
        recordingStartTimeStamp = System.currentTimeMillis()
        recorder!!.start()
    }

    fun stopRecordingAndReadData(
        sentence: Sentence,
        onError: (Int) -> Unit,
        onSuccess: (Recording) -> Unit
    ) {
        try {
            when {
                System.currentTimeMillis() - recordingStartTimeStamp <= 500 -> {
                    if (!suppressError) {
                        //too short
                        onError(3)
                    } else {
                        onError(0)
                    }
                    return
                }
                System.currentTimeMillis() - recordingStartTimeStamp < 10000 -> {
                    recorder!!.stop()
                }
                else -> {
                    if (!suppressError) {
                        //too long
                        onError(2)
                    } else {
                        onError(0)
                    }
                    return
                }
            }
        } catch (e: IllegalStateException) {
            if (!suppressError) {
                onError(1)
            } else {
                onError(0)
            }
            return
        } catch (e: RuntimeException) {
            if (!suppressError) {
                onError(1)
            } else {
                onError(0)
            }
            return
        }
        if (false) {
            MediaConverter.convertToFormat(fileHolder) {
                Timber.i("Conversion was successful")
                Timber.i("Conversion result: ${it.decodeToString()}")
                onSuccess(sentence.toRecording(it))
            }
        } else {
            onSuccess(sentence.toRecording(fileHolder.getByteArray()))
        }
        setupRecorder()
    }

    fun stop(suppressError: Boolean) {
        this.suppressError = suppressError
        recorder?.stop()
    }

    fun clean() {
        recorder?.reset()
        recorder?.release()
        recorder = null
        fileHolder.clear()
    }

}