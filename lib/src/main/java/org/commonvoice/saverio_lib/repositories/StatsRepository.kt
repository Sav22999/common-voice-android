package org.commonvoice.saverio_lib.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitStatsUpdate
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitUserAppUsageBody
import org.commonvoice.saverio_lib.api.responseBodies.ResponseAppUsage
import org.commonvoice.saverio_lib.api.responseBodies.ResponseDailyUsage
import org.commonvoice.saverio_lib.models.AppAction
import org.commonvoice.saverio_lib.models.Message
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

    suspend fun getAppUsageStats(
        language: String?,
        filter: String?,
        year: String?
    ): Map<String, ResponseAppUsage> {
        return statsClient.getAppUsageStatistics(language, filter, year).body() ?: mapOf()
    }

    suspend fun postAppUsageStatistics(
        appVersionCode: Int,
        appSource: String,
        appAction: AppAction
    ): Boolean = withContext(Dispatchers.IO) {
        val body = RetrofitUserAppUsageBody(
            isLogged(),
            appAction.language,
            appVersionCode,
            appSource,
            appAction.type.num,
            if (mainPrefManager.areAppUsageStatsEnabled) getUserId() else "",
            if (appAction.offline) 1 else 0,
            appAction.sentence_id,
            appAction.clip_id,
            appAction.details
        )

        try {
            statsClient.postAppUsageStatistics(body)
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun getUserAppUsageStatistics(
        userId: String,
        startDate: String? = null,
        endDate: String? = null
    ): ResponseAppUsage? {
        return statsClient.getUserAppUsageStatistics(userId, startDate, endDate).body()
    }

    fun getUserId(): String {
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

    suspend fun getNewMessages(): List<Message> {
        val response = statsClient.getNewMessages().body()
        return response
            ?.values
            ?.toList()
            ?.sortedByDescending { it.id }
            ?.filter {
                (it.startDateFilter == null || dateMillis(it.startDateFilter) <= currentMillis)
                        && (it.endDateFilter == null || (dateMillis(it.endDateFilter) + DAY_MILLIS) >= currentMillis)
                        && (it.userFilter == mainPrefManager.statsUserId || (it.userFilter == null
                        && (it.sourceFilter == null || it.sourceFilter.equals(
                    mainPrefManager.appSourceStore,
                    true
                ))
                        && (it.languageFilter == null || it.languageFilter.equals(
                    mainPrefManager.language,
                    true
                ))
                        && (it.versionCodeFilter == null || it.versionCodeFilter.toIntOrNull() == mainPrefManager.appVersionCode))
                        )
            }
            ?: emptyList()
    }

    private fun isLogged(): Int {
        return if (mainPrefManager.sessIdCookie == null) {
            0
        } else {
            1
        }
    }

    companion object {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        private fun dateMillis(date: String): Long = try {
            dateFormat.parse(date)?.time
        } catch (e: Exception) {
            null
        } ?: 0

        private const val DAY_MILLIS = 24 * 60 * 60 * 1000

        private val currentMillis: Long
            get() = System.currentTimeMillis()

    }

}