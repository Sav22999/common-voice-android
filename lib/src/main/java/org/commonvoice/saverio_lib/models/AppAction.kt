package org.commonvoice.saverio_lib.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appAction")
data class AppAction(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "action_id")
    val id: Int,

    @ColumnInfo(name = "language")
    val language: String,

    @ColumnInfo(name = "type")
    val type: Type,

    @ColumnInfo(name = "offline")
    val offline: Boolean,

    @ColumnInfo(name = "sentence_id")
    val sentence_id: String,

    @ColumnInfo(name = "clip_id")
    val clip_id: String,

    @ColumnInfo(name = "details")
    val details: String,

    ) {

    enum class Type(val num: Int) {
        LISTEN_REJECTED(0),
        LISTEN_ACCEPTED(1),
        LISTEN_REPORTED(2),

        SPEAK_SENT(3),
        SPEAK_REPORTED(4),
    }
}
