package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.api.requestBodies.RetrofitStatsUpdate
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitUserAppUsageBody
import org.commonvoice.saverio_lib.api.responseBodies.ResponseAppUsage
import org.commonvoice.saverio_lib.api.responseBodies.ResponseDailyUsage
import org.commonvoice.saverio_lib.models.Message
import retrofit2.Response
import retrofit2.http.*

interface StatsService {

    //TODO: this is the requests API page

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
        @Query("filter") filter: String?,
        @Query("year") year: String?
    ): Response<Map<String, ResponseAppUsage>>

    @POST("v2/app-usage/")
    suspend fun postAppUsageStatistics(
        @Body body: RetrofitUserAppUsageBody
    )

    @GET("v2/app-usage/get/")
    suspend fun getUserAppUsageStatistics(
        @Query("id") id: String,
        @Query("start_date") start_date: String?,
        @Query("end_date") end_date: String?
    ): Response<ResponseAppUsage>

    @GET("v2/messages/get")
    suspend fun getNewMessages(): Response<Map<String, Message>>

}