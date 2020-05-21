package org.commonvoice.saverio_lib.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
data class RecordableSentence(

    @Json(name = "text")
    val sentence: String,

    @Json(name = "id")
    val sentence_id: String,

    @Transient
    var file: File = File.createTempFile(UUID.randomUUID().toString(), null)

) : Parcelable {

    fun resetFile() {
        try {
            file.delete()
        } finally {
            file = File.createTempFile(UUID.randomUUID().toString(), null)
        }
    }

}