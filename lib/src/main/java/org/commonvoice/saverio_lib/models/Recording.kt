package org.commonvoice.saverio_lib.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.*

@Parcelize
data class Recording(

    val sentence: String,

    val sentence_id: String,

    val file: File = File.createTempFile(UUID.randomUUID().toString(), null)

) : Parcelable {

}