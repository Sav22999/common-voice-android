package org.commonvoice.saverio_lib.mediaRecorder

import android.media.*
import android.media.MediaCodecList.REGULAR_CODECS
import android.os.Build
import android.util.Log
import okio.ByteString.Companion.readByteString
import org.commonvoice.saverio_lib.mediaPlayer.ByteArrayDataSource
import timber.log.Timber
import java.io.*
import java.lang.IllegalStateException

object MediaConverter {

    private val codecList = MediaCodecList(REGULAR_CODECS)

    private val outputCodecName by lazy {
        codecList
            .codecInfos
            .filter {
                it.isEncoder
            }
            .find {
                it.supportedTypes.contains(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && false)
                        MediaFormat.MIMETYPE_AUDIO_OPUS
                    else
                        MediaFormat.MIMETYPE_AUDIO_FLAC
                )
            }
            ?.name ?: throw IOException("The required codecs are not available")
    }

    fun convertToFormat(input: FileHolder, onSuccess: (ByteArray) -> Unit) {
        val extractor = MediaExtractor()
        extractor.setDataSource(ByteArrayDataSource(input.getByteArray()))
        val inputFormat = extractor.getTrackFormat(0)
        val inputCodecName = codecList.findDecoderForFormat(inputFormat)
        val inputCodec = MediaCodec.createByCodecName(inputCodecName)

        val inputArray = ByteArrayInputStream(input.getByteArray())
        val temporaryArray = ByteArrayOutputStream()
        var temporaryInputArray: ByteArrayInputStream? = null
        val outputArray = ByteArrayOutputStream()

        val outputCodec = MediaCodec.createByCodecName(outputCodecName)
        val outputFormat = MediaFormat.createAudioFormat(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && false)
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
                if (inputArray.available() <= 0) {
                    codec.queueInputBuffer(index, 0, 0, 5000000, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    return
                }

                val buffer = codec.getInputBuffer(index)!!

                Timber.i("InputCodec InputBuffer available, ${buffer.remaining()}, $index")

                val written = if (buffer.remaining() < inputArray.available())
                    buffer.remaining()
                else
                    inputArray.available()
                val temp = ByteArray(written)
                inputArray.read(temp, 0, written)
                buffer.put(temp)

                Timber.i("InputCodec; filled buffer with $written bytes. Available bytes: ${inputArray.available()}")
                codec.queueInputBuffer(index, 0, written, 0, 0)
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                val buffer = codec.getOutputBuffer(index)!!
                val byteArray = ByteArray(buffer.remaining())
                Timber.i("InputCodec; received output buffer with ${info.size} bytes (offset: ${info.offset}). Flags: ${info.flags}")
                buffer.get(byteArray)
                temporaryArray.write(byteArray)
                codec.releaseOutputBuffer(index, false)
                if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Timber.i("Intermediate buffer size: ${temporaryArray.size()}")
                    temporaryInputArray = ByteArrayInputStream(temporaryArray.toByteArray())
                    temporaryArray.close()
                    outputCodec.start()
                    //inputCodec.stop()
                }
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Timber.i("MediaConverter output format changed. New format: ${format.getString(MediaFormat.KEY_MIME)}")
            }
        })

        outputCodec.setCallback(object: MediaCodec.Callback() {
            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Timber.e("MediaConverter error. Codec: ${codec.name}. Exception: ${e.diagnosticInfo}")
            }

            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                var size = 0
                if (temporaryInputArray!!.available() <= 0) {
                    codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    return
                }
                codec.getInputBuffer(index)?.let {
                    Timber.i("OutputCodec InputBuffer available, ${it.remaining()}, $index")
                    while (it.remaining() > 0 && temporaryInputArray!!.available() > 0) {
                        it.put(temporaryInputArray!!.read().toByte())
                        size++
                    }
                }
                Timber.i("OutputCodec; filled buffer with $size bytes. Available bytes: ${temporaryInputArray!!.available()}")
                codec.queueInputBuffer(index, 0, size, 0, 0)
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                val buffer = codec.getOutputBuffer(index)!!
                val byteArray = ByteArray(buffer.remaining())
                buffer.get(byteArray)
                outputArray.write(byteArray)
                codec.releaseOutputBuffer(index, false)
                /*if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    outputCodec.stop()
                    outputCodec.release()
                    //inputCodec.release()
                    extractor.release()
                    inputArray.close()
                    temporaryArray.close()
                    temporaryInputArray!!.close()
                    onSuccess(outputArray.toByteArray())
                    outputArray.close()
                }*/
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