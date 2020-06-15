package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.responseBodies.ResponseEverStats
import org.commonvoice.saverio_lib.preferences.MainPrefManager

@WorkerThread
class CVStatsRepository(
    private val mainPrefManager: MainPrefManager,
    retrofitFactory: RetrofitFactory
) {

    private val language: String
        get() = mainPrefManager.language

    private val cvStatsClient = retrofitFactory.makeCVStatsService()

    suspend fun getUserClient() = cvStatsClient.getUserClient().body()

    suspend fun getDailyClipsCount() = cvStatsClient.getDailyClipsCount(language).body() ?: 0

    suspend fun getDailyVotesCount() = cvStatsClient.getDailyVotesCount(language).body() ?: 0

    suspend fun getEverCount(): ResponseEverStats {
        return cvStatsClient.getEverCount(language).body()?.lastOrNull() ?: ResponseEverStats(0, 0)
    }

    suspend fun getHourlyVoices() = cvStatsClient.getHourlyVoices(language)

    suspend fun getRecordingsLeaderboard() = cvStatsClient.getRecordingsLeaderboard(language).body() ?: listOf()

    suspend fun getClipsLeaderboard() = cvStatsClient.getClipsLeaderboard(language).body() ?: listOf()

}