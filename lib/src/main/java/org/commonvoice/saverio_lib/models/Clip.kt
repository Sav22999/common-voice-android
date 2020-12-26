package org.commonvoice.saverio_lib.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Suppress("ArrayInDataClass")
@Entity(tableName = "clips")
data class Clip(

    @PrimaryKey
    @ColumnInfo(name = "clip_id")
    val id: String,

    @ColumnInfo(name = "glob")
    val glob: String,

    @Embedded
    val sentence: Sentence,

    @ColumnInfo(name = "audio", typeAffinity = ColumnInfo.BLOB)
    val audio: ByteArray = byteArrayOf()

) : Parcelable {

    fun toValidation(validationResult: Boolean) = Validation(
        id,
        sentence.language,
        validationResult,
        attempts = 1
    )

}