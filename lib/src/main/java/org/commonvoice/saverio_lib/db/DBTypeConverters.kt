package org.commonvoice.saverio_lib.db

import androidx.room.TypeConverter
import org.commonvoice.saverio_lib.models.AppAction
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

    @JvmStatic
    @TypeConverter
    fun actionTypeToInt(action: AppAction.Type) = action.ordinal

    @JvmStatic
    @TypeConverter
    fun intToActionType(action: Int) = AppAction.Type.values()[action]

}