package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.models.RecordableSentence
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SentenceService {

    @Headers("Accept-Type: application/json")
    @GET("sentences")
    suspend fun getSentence(): Response<List<RecordableSentence>>

}