package org.commonvoice.saverio_lib.api.services

import okhttp3.RequestBody
import org.commonvoice.saverio_lib.api.responses.RecordingResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface RecordingService {

    @POST("clips")
    @Headers("Content-Type: audio/mpeg; codecs=aac")
    suspend fun sendRecording(
        @Header("sentence") sentence: String,
        @Header("sentence_id") id: String,
        @Body rawBody: RequestBody
    ): Response<RecordingResult>

}