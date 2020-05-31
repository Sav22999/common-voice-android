package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.models.Report
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ReportsService {

    @Headers("Accept-Type: application/json")
    @POST("reports")
    suspend fun sendReport(@Body reportData: Report)


}