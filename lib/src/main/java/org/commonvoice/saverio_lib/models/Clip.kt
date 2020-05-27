package org.commonvoice.saverio_lib.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "clips")
@Parcelize
@JsonClass(generateAdapter = true)
data class Clip(

    @PrimaryKey
    @ColumnInfo(name = "clip_id")
    @Json(name = "id")
    val id: String,

    @ColumnInfo(name = "glob")
    @Json(name = "glob")
    val glob: String,

    @ColumnInfo(name = "audio_src")
    @Json(name = "audioSrc")
    val audioSrc: String,

    @Embedded
    @Json(name = "sentence")
    val sentence: Sentence

): Parcelable