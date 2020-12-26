package org.commonvoice.saverio_lib.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.sql.Timestamp

@Parcelize
@Suppress("ArrayInDataClass")
@Entity(tableName = "recordings")
data class Recording(

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
    val expiryDate: Timestamp = getTimestampOfNowPlus(days = 7),

    @ColumnInfo(name = "attempts")
    val attempts: Int

) : Parcelable {

    fun increaseAttempt(): Recording = this.copy(attempts = attempts + 1)

}