package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.api.requestBodies.RetrofitValidationBody
import org.commonvoice.saverio_lib.api.responseBodies.RetrofitValidationResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ValidationsService {

    @POST("{language}/clips/{sentence_id}/votes")
    suspend fun sendValidation(
        @Path("language") language: String,
        @Path("sentence_id") sentenceId: String,
        @Body validationBody: RetrofitValidationBody
    ): Response<RetrofitValidationResult>

}