package org.commonvoice.saverio_lib.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.sql.Timestamp

@Entity(tableName = "validations")
data class Validation(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "lang")
    val language: String,

    @ColumnInfo(name = "valid")
    val isValid: Boolean,

    @ColumnInfo(name = "expiry")
    @Transient
    val expiryDate: Timestamp = getTimestampOfNowPlus(days = 7),

    @ColumnInfo(name = "attempts")
    val attempts: Int

) {

    fun increaseAttempt() = this.copy(attempts = attempts + 1)

}