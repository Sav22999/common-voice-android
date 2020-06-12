package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.models.Sentence
import retrofit2.Response
import retrofit2.http.*

interface SentencesService {

    @Headers("Accept-Type: application/json")
    @GET("sentences")
    suspend fun getSentence(@Query("count") count: Int): Response<List<Sentence>>

}