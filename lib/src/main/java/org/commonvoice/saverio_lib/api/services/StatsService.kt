package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.api.requestBodies.RetrofitStatsUpdate
import org.commonvoice.saverio_lib.api.responseBodies.ResponseDailyUsage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface StatsService {

    @Headers("Content-Type: application/json")
    @POST("v2/")
    suspend fun postStats(@Body stats: RetrofitStatsUpdate)

    @GET("v2/app-statistics/")
    suspend fun getStats(): Response<Map<String, ResponseDailyUsage>>

}