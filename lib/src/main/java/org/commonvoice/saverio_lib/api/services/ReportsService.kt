package org.commonvoice.saverio_lib.api.services

import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ReportsService {

    @Headers("Accept-Type: application/json", "kind: sentence")
    @POST("reports")
    suspend fun reportSentence(
        @Header("reasons") reasons: List<String>,
        @Header("id") id: String
    )

}