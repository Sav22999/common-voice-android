package org.commonvoice.saverio_lib.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "reports")
@JsonClass(generateAdapter = true)
data class Report(

    @PrimaryKey
    @Json(name = "id")
    val sentenceId: String,

    @ColumnInfo(name = "kind")
    @Json(name = "kind")
    val name: String,

    @ColumnInfo(name = "reasons")
    @Json(name = "reasons")
    val reasons: List<String>

) {

    constructor(sentence: Sentence, reasons: List<String>) : this(sentence.sentenceId, "sentence", reasons)

    constructor(clip: Clip, reasons: List<String>) : this(clip.id, "clip", reasons)

}