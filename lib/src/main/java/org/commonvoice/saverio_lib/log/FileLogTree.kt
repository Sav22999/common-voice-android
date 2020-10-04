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

        if (priority == Log.DEBUG) {
            try {
                val directory =
                    File(
                        ctx.getExternalFilesDir(
                            null
                        ), "logs"
                    )
//                    Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_DOCUMENTS}/logs")

                if (!directory.exists()) {
                    Log.d("FileLogTree","no exist")
                    directory.mkdirs()
                } else {
                    Log.d("FileLogTree","exist")
                }

                val fileName = "log.txt"

//                Timber.d("Path: ${directory.absolutePath}")
                val file = File("${directory.absolutePath}${File.separator}$fileName")

                file.createNewFile()

                if (file.exists()) {
                    val fos = FileOutputStream(file, true)

                    fos.write("$message\n".toByteArray(Charsets.UTF_8))
                    fos.close()
                }

            } catch (e: IOException) {
                Timber.e("Error while logging into file: $e")
            }
        }
    }
}