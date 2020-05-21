package org.commonvoice.saverio_lib.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.commonvoice.saverio_lib.api.okhttp.AuthenticationInterceptor
import org.commonvoice.saverio_lib.api.services.ClipService
import org.commonvoice.saverio_lib.api.services.ReportsService
import org.commonvoice.saverio_lib.api.services.SentenceService
import org.commonvoice.saverio_lib.utils.PrefManager
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * RetrofitFactory injects our AuthenticationInterceptor inside the OkHttp client used by Retrofit
 * and provides the implementations of the various services to the repositories
 */
class RetrofitFactory(prefManager: PrefManager) {

    private val genericURL = "https://voice.allizom.org/api/v1/"
    private val langURL = genericURL + prefManager.language + "/"

    private val baseRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthenticationInterceptor(prefManager))
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
        )

    private val langRetrofit = baseRetrofit.baseUrl(langURL).build()
    private val genericRetrofit = baseRetrofit.baseUrl(genericURL).build()
    private val testingRetrofit = baseRetrofit.baseUrl("http://192.168.1.227:5000/").build()

    fun makeClipService(): ClipService = langRetrofit.create(ClipService::class.java)

    fun makeSentenceService(): SentenceService = langRetrofit.create(SentenceService::class.java)

    fun makeReportsService(): ReportsService = genericRetrofit.create(ReportsService::class.java)

}