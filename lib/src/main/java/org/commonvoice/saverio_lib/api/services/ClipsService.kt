package org.commonvoice.saverio_lib.api.services

import okhttp3.ResponseBody
import org.commonvoice.saverio_lib.api.responseBodies.RetrofitClip
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

interface ClipsService {

    @Headers("Accept-Type: application/json")
    @GET("clips")
    suspend fun getClips(@Query("count") count: Int): Response<List<RetrofitClip>>

}