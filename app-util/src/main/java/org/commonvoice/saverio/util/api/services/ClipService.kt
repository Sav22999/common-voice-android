package org.commonvoice.saverio.util.api.services

import okhttp3.RequestBody
import org.commonvoice.saverio.util.api.responses.ClipResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ClipService {

    @POST("clips")
    @Headers("Content-Type: audio/ogg; codecs=opus4", "Accept: application/json", "client_id: commonvoiceandroidprova")
    suspend fun sendClip(
        @Header("sentence") sentence: String,
        @Header("sentence_id") id: String,
        @Header("Authorization") auth: String?,
        @Header("Cookie") cookie: String?,
        @Header("Content-Length") length: Int,
        @Body sound: RequestBody
    ): Response<ClipResponse>

}