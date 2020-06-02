package org.commonvoice.saverio_lib.api.services

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ClipsDownloadService {

    @GET
    suspend fun downloadAudioFile(@Url audioSrc: String): Response<ResponseBody>

}