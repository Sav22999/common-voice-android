package org.commonvoice.saverio_lib.mediaRecorder

import android.content.Context
import org.commonvoice.saverio_lib.utils.openDirectory
import org.commonvoice.saverio_lib.utils.openFile
import java.io.FileDescriptor
import java.util.*

//https://stackoverflow.com/questions/13974234/android-record-mic-to-bytearray-without-saving-audio-file/42750515
class FileHolder(context: Context) {

    private val path = context.cacheDir.openDirectory("mediaRecorder")

    private val randomUUID: String
        get() = UUID.randomUUID().toString()

    private var file = path.openFile(randomUUID)

    val fileDescriptor: FileDescriptor
        get() = file.outputStream().fd

    fun reset() {
        file.delete()
        file = path.openFile(randomUUID)
    }

    fun clear() {
        file.delete()
    }

    fun getByteArray(): ByteArray = file.readBytes()

}