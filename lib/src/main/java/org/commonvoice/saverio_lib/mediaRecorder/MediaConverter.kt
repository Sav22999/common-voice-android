package org.commonvoice.saverio_lib.mediaRecorder

import android.media.*
import android.media.MediaCodecList.REGULAR_CODECS
import android.os.Build
import android.util.Log
import okio.ByteString.Companion.readByteString
import org.commonvoice.saverio_lib.mediaPlayer.ByteArrayDataSource
import timber.log.Timber
import java.io.*

object MediaConverter {

    private val extractor = MediaExtractor()
    private val codecList = MediaCodecList(REGULAR_CODECS)

    private val outputCodecName by lazy {
        codecList
            .codecInfos
            .filter {
                it.isEncoder
            }
            .find {
                it.supportedTypes.contains(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        MediaFormat.MIMETYPE_AUDIO_OPUS
                    else
                        MediaFormat.MIMETYPE_AUDIO_FLAC
                )
            }
            ?.name ?: ""
    }

    fun convertToFormat(input: FileHolder, onSuccess: (ByteArray) -> Unit) {
        extractor.setDataSource(ByteArrayDataSource(input.getByteArray()))
        val inputFormat = extractor.getTrackFormat(0)
        val inputCodecName = codecList.findDecoderForFormat(inputFormat)
        val inputCodec = MediaCodec.createByCodecName(inputCodecName)

        val inputArray = ByteArrayInputStream(input.getByteArray())
        val temporaryArray = ByteArrayOutputStream()
        val temporaryInputArray = ByteArrayInputStream(temporaryArray.toByteArray())
        val outputArray = ByteArrayOutputStream()

        val outputCodec = MediaCodec.createByCodecName(outputCodecName)
        val outputFormat = MediaFormat.createAudioFormat(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaFormat.MIMETYPE_AUDIO_OPUS
            else
                MediaFormat.MIMETYPE_AUDIO_FLAC,
            44100,
            1
        ).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 65536)
            setInteger(MediaFormat.KEY_FLAC_COMPRESSION_LEVEL, 5)
            setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_OUT_MONO)
        }

        inputCodec.setCallback(object: MediaCodec.Callback() {
            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Timber.e("MediaConverter error. Codec: ${codec.name}. Exception: ${e.diagnosticInfo}")
            }

            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                var size = 0
                codec.getInputBuffer(index)?.let {
                    Timber.e("Provola, ${it.remaining()}, $index")
                    while (it.remaining() > 0 && inputArray.available() > 0) {
                        it.putChar(inputArray.read().toChar())
                        size++
                    }
                }
                codec.queueInputBuffer(index, 0, size, 0, if (inputArray.available() > 0) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                val buffer = codec.getOutputBuffer(index)!!
                val byteArray = ByteArray(buffer.remaining())
                buffer.get(byteArray)
                temporaryArray.write(byteArray)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    outputCodec.start()
                    inputCodec.stop()
                }
                codec.releaseOutputBuffer(index, 0)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Timber.e("MediaConverter error. Output format changed.")
            }
        })

        outputCodec.setCallback(object: MediaCodec.Callback() {
            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Timber.e("MediaConverter error. Codec: ${codec.name}. Exception: ${e.diagnosticInfo}")
            }

            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                val buffer = codec.getInputBuffer(index)!!
                var eos = true
                val size = if (buffer.capacity() < temporaryInputArray.available()) {
                    eos = false
                    buffer.capacity()
                } else temporaryInputArray.available()
                temporaryInputArray.readByteString(size).asByteBuffer().let {
                    buffer.put(it)
                }
                codec.queueInputBuffer(index, 0, size, 0, if (eos) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0)
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                val buffer = codec.getOutputBuffer(index)
                outputArray.write(buffer?.array())
                codec.releaseOutputBuffer(index, 0)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    outputCodec.stop()
                    outputCodec.release()
                    inputCodec.release()
                    inputArray.close()
                    temporaryArray.close()
                    temporaryInputArray.close()
                    onSuccess(outputArray.toByteArray())
                    outputArray.close()
                }
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Timber.e("MediaConverter error. Output format changed.")
            }
        })

        try {
            inputCodec.configure(
                inputFormat,
                null,
                null,
                0
            )

            outputCodec.configure(
                outputFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
        } catch (e: MediaCodec.CodecException) {
            Timber.e(e)
            Timber.e("Error: ${e.diagnosticInfo}, recoverable: ${e.isRecoverable}, transient: ${e.isTransient}")
        }

        inputCodec.start()
    }

}