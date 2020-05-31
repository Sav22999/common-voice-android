package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.api.retrofitModels.RetrofitReport
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ReportsService {

    @Headers("Accept-Type: application/json")
    @POST("reports")
    suspend fun sendReport(@Body reportData: RetrofitReport)

}