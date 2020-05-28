package org.commonvoice.saverio_lib.utils

import java.sql.Timestamp

fun getTimestampOfNowPlus(days: Int = 0,
                          hours: Int = 0,
                          minutes: Int = 0,
                          seconds: Int = 0): Timestamp {
    val currentMillis = System.currentTimeMillis()
    var additionalMillis = days
    additionalMillis = additionalMillis * 24 + hours
    additionalMillis = additionalMillis * 60 + minutes
    additionalMillis = additionalMillis * 60 + seconds
    additionalMillis *= 1000
    return Timestamp(currentMillis + additionalMillis)
}