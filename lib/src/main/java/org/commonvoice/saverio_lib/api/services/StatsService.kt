package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.api.requestBodies.RetrofitStatsUpdate
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitUserAppUsageBody
import org.commonvoice.saverio_lib.api.responseBodies.ResponseAppUsage
import org.commonvoice.saverio_lib.api.responseBodies.ResponseDailyUsage
import retrofit2.Response
import retrofit2.http.*

interface StatsService {

    @Headers("Content-Type: application/json")
    @POST("v2/")
    suspend fun postStats(@Body stats: RetrofitStatsUpdate)

    @GET("v2/app-statistics/")
    suspend fun getStats(): Response<Map<String, ResponseDailyUsage>>

    @GET("v2/app-statistics/{language}/")
    suspend fun getLanguageSpecificStats(
        @Path("language") language: String
    ): Response<Map<String, ResponseDailyUsage>>

    @GET("v2/app-usage/get/")
    suspend fun getAppUsageStatistics(
        @Query("language") language: String?,
        @Query("year") year: String?
    ): Response<Map<String, ResponseAppUsage>>

    @POST("v2/app-usage/")
    suspend fun postAppUsageStatistics(
        @Body body: RetrofitUserAppUsageBody
    )

}