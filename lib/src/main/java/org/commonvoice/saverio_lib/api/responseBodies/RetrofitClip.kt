package org.commonvoice.saverio_lib.api.responseBodies

import androidx.room.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.models.Sentence

@JsonClass(generateAdapter = true)
data class RetrofitClip(

    @Json(name = "id")
    val id: String,

    @Json(name = "glob")
    val glob: String,

    @Json(name = "sentence")
    val sentence: Sentence,

    @Ignore
    @Json(name = "audioSrc")
    val audioSrc: String = ""

) {

    fun toClip(audioBytes: ByteArray) = Clip(id, glob, sentence, audioBytes)

}