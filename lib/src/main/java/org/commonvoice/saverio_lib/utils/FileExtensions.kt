package org.commonvoice.saverio_lib.utils

import java.io.File

@Throws(RuntimeException::class)
fun File.openDirectory(dirName: String): File {
    if (!this.isDirectory) {
        throw RuntimeException("Parent File must be a directory")
    }

    return this.resolve(dirName).also {
        if (!it.exists()) {
            it.mkdir()
        }

        if (!it.isDirectory) {
            throw RuntimeException("$dirName is not a directory")
        }
    }
}

@Throws(RuntimeException::class)
fun File.openFile(fileName: String): File {
    if (!this.isDirectory) {
        throw RuntimeException("Parent File must be a directory")
    }

    return this.resolve(fileName).also {
        if (!it.exists()) {
            it.createNewFile()
        }

        if (!it.isFile) {
            throw RuntimeException("$fileName is not a file")
        }
    }
}