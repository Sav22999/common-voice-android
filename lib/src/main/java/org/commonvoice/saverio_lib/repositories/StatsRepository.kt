package org.commonvoice.saverio_lib.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitStatsUpdate
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitUserAppUsageBody
import org.commonvoice.saverio_lib.api.responseBodies.ResponseAppUsage
import org.commonvoice.saverio_lib.api.responseBodies.ResponseDailyUsage
import org.commonvoice.saverio_lib.models.AppAction
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import timber.log.Timber
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class StatsRepository(
    private val mainPrefManager: MainPrefManager,
    retrofitFactory: RetrofitFactory
) {

    private val statsClient = retrofitFactory.makeStatsService()

    private var lastStatsUpdateTime: Timestamp = Timestamp(0)

    suspend fun postStatsUpdate(
        appVersion: String,
        versionCode: String,
        appSource: String
    ) = withContext(Dispatchers.IO) {
        if (appVersion.contains('a') || appVersion.contains('b')) return@withContext

        val timeOfNow = getTimestampOfNowPlus(seconds = 0)

        if (timeOfNow.time - lastStatsUpdateTime.time >= 30000) { //At least 30 seconds have passed since the last stats update
            lastStatsUpdateTime = timeOfNow

            val stats = RetrofitStatsUpdate(
                getUserId(),
                isLogged(),
                mainPrefManager.language,
                versionCode,
                mainPrefManager.areGenericStats.toString(),
                appSource
            )

            try {
                statsClient.postStats(stats)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    suspend fun getStats(): Map<String, ResponseDailyUsage> =
        statsClient.getStats().body() ?: mapOf()

    suspend fun getLanguageSpecificStats(language: String): ResponseDailyUsage? {
        return statsClient.getLanguageSpecificStats(language).body()?.get(language)
    }

    suspend fun getAppUsageStats(language: String?, year: String?): Map<String, ResponseAppUsage> {
        return statsClient.getAppUsageStatistics(language, year).body() ?: mapOf()
    }

    suspend fun postAppUsageStatistics(
        appVersionCode: Int,
        appSource: String,
        appAction: AppAction
    ) = withContext(Dispatchers.IO) {
        val body = RetrofitUserAppUsageBody(
            isLogged(),
            appAction.language,
            appVersionCode,
            appSource,
            appAction.type.num,
            mainPrefManager.username,
            if (appAction.offline) 1 else 0
        )

        try {
            statsClient.postAppUsageStatistics(body)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun getUserId(): String {
        val userId = mainPrefManager.statsUserId

        return if (userId != "") {
            userId
        } else {
            val dateTemp = SimpleDateFormat("yyyyMMddHHmmssSSSS")
            val dateTime = dateTemp.format(Date()).toString()
            val uniqueUserId = "User$dateTime::CVAppSav"
            mainPrefManager.statsUserId = uniqueUserId
            uniqueUserId
        }
    }

    private fun isLogged(): Int {
        return if (mainPrefManager.sessIdCookie == null) {
            0
        } else {
            1
        }
    }


}