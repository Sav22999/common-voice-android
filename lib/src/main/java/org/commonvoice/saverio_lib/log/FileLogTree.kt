package org.commonvoice.saverio_lib.log

import android.content.Context
import android.os.Environment
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileLogTree(val ctx: Context) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        if (priority == Log.ERROR) {
            try {
                val directory =
                    File(
                        ctx.getExternalFilesDir(
                            null
                        ), "logs"
                    )

                if (!directory.exists()) {
                    directory.mkdirs()
                }

                val fileName = "log.txt"
                val oldFileName = "log_old.txt"

                val file = File("${directory.absolutePath}${File.separator}$fileName")
                val oldFile = File("${directory.absolutePath}${File.separator}$oldFileName")

//                file.createNewFile()

                if (file.exists()) {
                    file.renameTo(oldFile)
                    file.createNewFile()
                    writeLogToFile(file, message)
                } else {
                    file.createNewFile()
                    writeLogToFile(file, message)
                }

            } catch (e: IOException) {
                Timber.e("Error while logging into file: $e")
            }
        }
    }

    private fun writeLogToFile(file: File, message: String) {
        val fos = FileOutputStream(file, true)

        fos.write("$message\n".toByteArray(Charsets.UTF_8))
        fos.close()
    }
}