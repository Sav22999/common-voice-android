package org.commonvoice.saverio.util.api

import org.commonvoice.saverio.util.api.services.ClipService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactory {

    //const val baseURL = "https://voice.mozilla.org/api/v1/{{*{{lang}}*}}/"
    //const val baseURL = "https://voice-web.mone27.net/api/v1/it/"
    const val baseURL = "http://192.168.1.227:5000/"

    private fun makeRetrofit() = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .baseUrl(baseURL)
        .build()

    fun makeClipService(): ClipService = makeRetrofit().create(ClipService::class.java)

}