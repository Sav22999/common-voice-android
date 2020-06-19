package org.commonvoice.saverio_lib.mediaPlayer

import android.media.MediaDataSource
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.models.Recording

class ByteArrayDataSource(
    private val audioFile: ByteArray
): MediaDataSource() {

    constructor(recording: Recording): this(recording.audio)
    constructor(clip: Clip): this(clip.audio)

    override fun close() {
        //Nothing to do here
    }

    override fun getSize(): Long = audioFile.size.toLong()

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {

        if (buffer == null || size == 0) {
            return 0
        }

        if (position > audioFile.size) {
            return -1
        }

        val readSize = if ((position.toInt() + size) > audioFile.size) {
            audioFile.size - position.toInt()
        } else {
            size
        }

        audioFile.copyInto(
            buffer,
            offset,
            position.toInt(),
            position.toInt() + readSize
        )

        return readSize
    }

}