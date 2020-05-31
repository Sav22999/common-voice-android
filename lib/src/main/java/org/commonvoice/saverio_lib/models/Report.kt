package org.commonvoice.saverio_lib.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.sql.Timestamp

@Entity(tableName = "reports")
data class Report(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "sentence_id")
    var sentenceId: String = "",

    @ColumnInfo(name = "kind")
    var kind: String = "",

    @ColumnInfo(name = "reasons")
    var reasons: List<String> = listOf(),

    @ColumnInfo(name = "expiry_date")
    var expiryDate: Timestamp = getTimestampOfNowPlus(days = 7)

) {

    constructor(sentence: Sentence, reasons: List<String>) : this(0, sentence.sentenceId, "sentence", reasons, getTimestampOfNowPlus(days = 7))

    constructor(clip: Clip, reasons: List<String>) : this(0, clip.id, "clip", reasons, getTimestampOfNowPlus(days = 7))

}