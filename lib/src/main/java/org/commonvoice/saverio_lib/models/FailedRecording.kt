package org.commonvoice.saverio_lib.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.sql.Timestamp

@Suppress("ArrayInDataClass")
@Entity(tableName = "failedRecordings")
data class FailedRecording(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val sentenceId: String,

    @ColumnInfo(name = "text")
    val sentenceText: String,

    @ColumnInfo(name = "lang")
    val language: String,

    @ColumnInfo(name = "audio", typeAffinity = ColumnInfo.BLOB)
    val audio: ByteArray,

    @ColumnInfo(name = "expiry")
    val expiryDate: Timestamp,

    @ColumnInfo(name = "tries")
    var failedPostsNumber: Int

) {

    fun toRecording(): Recording = Recording(sentenceId, sentenceText, language, audio, expiryDate)

}