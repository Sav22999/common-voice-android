package org.commonvoice.saverio_lib.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.sql.Timestamp

@Entity(tableName = "sentences")
@Parcelize
@JsonClass(generateAdapter = true)
data class Sentence(

    @PrimaryKey
    @ColumnInfo(name = "id")
    @Json(name = "id")
    val sentenceId: String,

    @ColumnInfo(name = "text")
    @Json(name = "text")
    val sentenceText: String,

    @ColumnInfo(name = "lang")
    @Transient
    var language: String = "en",

    @ColumnInfo(name = "expiry")
    @Transient
    val expiryDate: Timestamp = getTimestampOfNowPlus(days = 7)

) : Parcelable {

    fun setLanguage(lang: String) = this.also {
        language = lang
    }

    fun toRecording(array: ByteArray) = Recording(
        sentenceId,
        sentenceText,
        language,
        array,
        expiryDate,
        1
    )

}