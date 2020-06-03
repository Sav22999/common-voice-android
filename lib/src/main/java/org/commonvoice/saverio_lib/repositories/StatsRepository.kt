package org.commonvoice.saverio_lib.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitStatsUpdate
import org.commonvoice.saverio_lib.preferences.PrefManager
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class StatsRepository(
    private val prefManager: PrefManager,
    retrofitFactory: RetrofitFactory
) {

    private val statsClient = retrofitFactory.makeStatsService()

    private var lastStatsUpdateTime: Timestamp = Timestamp(0)

    suspend fun postStatsUpdate(appVersion: String, appSource: String) = withContext(Dispatchers.IO) {
        if (appVersion.contains('a') || appVersion.contains('b')) return@withContext

        val timeOfNow = getTimestampOfNowPlus(seconds = 0)

        if (timeOfNow.time - lastStatsUpdateTime.time >= 30000) { //At least 30 seconds have passed since the last stats update
            lastStatsUpdateTime = timeOfNow

            val stats = RetrofitStatsUpdate(
                getUserId(),
                isLogged(),
                prefManager.language,
                appVersion,
                prefManager.areStatsAnonymous.toString(),
                appSource
            )

            try {
                statsClient.postStats(stats)
            } catch (e: Exception) {
                Log.e("StatsRepository", "Error: $e. Probably Internet is not available.")
            }
        }
    }

    private fun getUserId(): String {
        val userId = prefManager.statsUserId

        return if (userId != "") {
            userId
        } else {
            val dateTemp = SimpleDateFormat("yyyyMMddHHmmssSSSS")
            val dateTime = dateTemp.format(Date()).toString()
            val uniqueUserId = "User$dateTime::CVAppSav"
            prefManager.statsUserId = uniqueUserId
            uniqueUserId
        }
    }

    private fun isLogged(): Int {
        return if (prefManager.sessIdCookie == null) {
            0
        } else {
            1
        }
    }

}