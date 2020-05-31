package org.commonvoice.saverio_lib.db

import androidx.room.TypeConverter
import java.sql.Timestamp

object DBTypeConverters {

    @JvmStatic
    @TypeConverter
    fun timestampToLong(time: Timestamp) = time.time

    @JvmStatic
    @TypeConverter
    fun longToTimestamp(timeMillis: Long) = Timestamp(timeMillis)

    @JvmStatic
    @TypeConverter
    fun stringListToString(list: List<String>) = list.joinToString(",")

    @JvmStatic
    @TypeConverter
    fun stringToStringList(list: String) = list.split(',')

}