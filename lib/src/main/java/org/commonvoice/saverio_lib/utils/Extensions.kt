package org.commonvoice.saverio_lib.utils

import java.sql.Timestamp

fun getTimestampOfNowPlus(days: Int): Timestamp {
    val currentMillis = System.currentTimeMillis() + days * 24 * 60 * 60 * 1000
    //days * hours * minutes * seconds * milliseconds
    return Timestamp(currentMillis)
}