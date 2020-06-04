package org.commonvoice.saverio_lib.models

import android.os.Parcelable
import androidx.room.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

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
)