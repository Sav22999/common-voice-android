package org.commonvoice.saverio_lib.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.commonvoice.saverio_lib.api.okhttp.AuthenticationInterceptor
import org.commonvoice.saverio_lib.api.services.SentenceService
import org.commonvoice.saverio_lib.utils.PrefManager
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * RetrofitFactory injects our AuthenticationInterceptor inside the OkHttp client used by Retrofit
 * and provides the implementations of the various services to the repositories
 */
class RetrofitFactory(prefManager: PrefManager) {

    //private val baseURL = "http://voice.allizom.org/api/v1/" + prefManager.language + "/"
    private val baseURL = "http://192.168.1.227:5000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseURL)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthenticationInterceptor(prefManager))
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
        )
        .build()

    fun makeSentenceService(): SentenceService = retrofit.create(SentenceService::class.java)

}