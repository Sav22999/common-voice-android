package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.preferences.MainPrefManager

@WorkerThread
class CVStatsRepository(
    mainPrefManager: MainPrefManager,
    retrofitFactory: RetrofitFactory
) {

    private val language = mainPrefManager.language

    private val cvStatsClient = retrofitFactory.makeCVStatsService()

    suspend fun getUserClient() = cvStatsClient.getUserClient()

    suspend fun getDailyClipsCount() = cvStatsClient.getDailyClipsCount(language)

    suspend fun getDailyVotesCount() = cvStatsClient.getDailyVotesCount(language)

    suspend fun getEverCount() = cvStatsClient.getEverCount(language)

    suspend fun getHourlyVoices() = cvStatsClient.getHourlyVoices(language)

}