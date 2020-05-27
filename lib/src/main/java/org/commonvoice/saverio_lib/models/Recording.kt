package org.commonvoice.saverio_lib.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.io.File
import java.sql.Timestamp

@Entity(tableName = "recordings")
data class Recording(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val sentenceId: String,

    @ColumnInfo(name = "text")
    val sentenceText: String,

    @ColumnInfo(name = "lang")
    val language: String,

    @ColumnInfo(name = "audio")
    val audio: String,

    @ColumnInfo(name = "expiry")
    @Transient
    var expiryDate: Timestamp = getTimestampOfNowPlus(15)

) {

    val requestBody: RequestBody
        get() {
            val file = File(audio)
            return file.asRequestBody("audio/mpeg".toMediaType())
        }

}