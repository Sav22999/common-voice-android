package org.commonvoice.saverio_lib.api.services

import okhttp3.RequestBody
import org.commonvoice.saverio_lib.api.responseBodies.RetrofitRecordingResult
import retrofit2.Response
import retrofit2.http.*

interface RecordingsService {

    @POST("{language}/clips")
    @Headers("Content-Type: audio/mpeg; codecs=aac")
    suspend fun sendRecording(
        @Path("language") language: String,
        @Header("sentence") sentence: String,
        @Header("sentence_id") id: String,
        @Body rawBody: RequestBody
    ): Response<RetrofitRecordingResult>

}