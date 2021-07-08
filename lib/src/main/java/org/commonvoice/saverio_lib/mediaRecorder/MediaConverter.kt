package org.commonvoice.saverio_lib.mediaRecorder

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaCodecList.REGULAR_CODECS
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import okio.ByteString.Companion.readByteString
import timber.log.Timber
import java.io.*

object MediaConverter {

    private val extractor = MediaExtractor()
    private val codecList = MediaCodecList(REGULAR_CODECS)

    fun convertToFormat(input: FileHolder): ByteArray {
        extractor.setDataSource(input.fileDescriptor)
        val inputFormat = extractor.getTrackFormat(0)
        val inputCodecName = codecList.findDecoderForFormat(inputFormat)
        val inputCodec = MediaCodec.createByCodecName(inputCodecName)

        val inputArray = ByteArrayInputStream(input.getByteArray())
        val temporaryArray = ByteArrayOutputStream()
        val outputArray = ByteArrayOutputStream()

        val outputCodec = MediaCodec.createByCodecName(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                "ogg/opus"
            else
                "audio/wav"
        )
        val outputFormat = extractor.getTrackFormat(0).apply {
            setString(MediaFormat.KEY_MIME,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    "ogg/opus"
                else
                    "audio/wav"
            )
            setInteger(MediaFormat.KEY_BIT_RATE, 65536)
            setInteger(MediaFormat.KEY_IS_ADTS, 0)

        }

        inputCodec.setCallback(object: MediaCodec.Callback() {
            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Timber.e("MediaConverter error. Codec: ${codec.name}. Exception: ${e.diagnosticInfo}")
            }

            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                val buffer = codec.getInputBuffer(index)!!
                var eos = false
                val size = if (buffer.capacity() > inputArray.available()) {
                    eos = true
                    buffer.capacity()
                } else inputArray.available()
                inputArray.readByteString(size).asByteBuffer().let {
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
                temporaryArray.write(buffer?.array())
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    outputCodec.start()
                }
                codec.releaseOutputBuffer(index, 0)
                inputCodec.stop()
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Timber.e("MediaConverter error. Output format changed.")
            }
        })

        outputCodec.setCallback(object: MediaCodec.Callback() {
            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                TODO("Not yet implemented")
            }

            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                TODO("Not yet implemented")
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    outputCodec.stop()
                }
                outputCodec.release()
                inputCodec.release()
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                TODO("Not yet implemented")
            }
        })

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
            0
        )

        inputCodec.start()

        return outputArray.toByteArray()
    }

}