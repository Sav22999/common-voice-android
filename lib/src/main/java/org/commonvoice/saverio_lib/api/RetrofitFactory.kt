package org.commonvoice.saverio_lib.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.commonvoice.saverio_lib.api.auth.AuthenticationInterceptor
import org.commonvoice.saverio_lib.api.network.TryCatchInterceptor
import org.commonvoice.saverio_lib.api.services.*
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitFactory injects our AuthenticationInterceptor inside the OkHttp client used by Retrofit
 * and provides the implementations of the various services to the repositories
 */
class RetrofitFactory(mainPrefManager: MainPrefManager) {

    private val genericURL = mainPrefManager.genericAPIUrl

    private val langURL = genericURL + mainPrefManager.language + "/"

    private val baseRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create().withNullSerialization())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthenticationInterceptor(mainPrefManager))
                .addInterceptor(TryCatchInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
        )

    private val unauthRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(TryCatchInterceptor())
                .build()
        )
        .baseUrl(statsURL)
        .build()

    private val langRetrofit = baseRetrofit.baseUrl(langURL).build()

    private val genericRetrofit = baseRetrofit.baseUrl(genericURL).build()

    fun makeRecordingService(): RecordingsService =
        genericRetrofit.create(RecordingsService::class.java)

    fun makeSentenceService(): SentencesService = langRetrofit.create(SentencesService::class.java)

    fun makeReportsService(): ReportsService = genericRetrofit.create(ReportsService::class.java)

    fun makeStatsService(): StatsService = unauthRetrofit.create(StatsService::class.java)

    fun makeLanguagesService(): LanguagesService =
        unauthRetrofit.create(LanguagesService::class.java)

    fun makeCVStatsService(): CVStatsService = genericRetrofit.create(CVStatsService::class.java)

    fun makeClipsService(): ClipsService = langRetrofit.create(ClipsService::class.java)

    fun makeValidationsService(): ValidationsService =
        genericRetrofit.create(ValidationsService::class.java)

    fun makeClipsDownloadService(): ClipsDownloadService =
        unauthRetrofit.create(ClipsDownloadService::class.java)

    fun makeGithubService(): GithubService = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .baseUrl(githubURL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(TryCatchInterceptor())
                .build()
        )
        .build()
        .create(GithubService::class.java)

    companion object {
        private const val statsURL = "https://www.saveriomorelli.com/api/common-voice-android/"
        private const val githubURL = "https://api.github.com/repos/Sav22999/common-voice-android/"
    }

    fun makeFileLogService(): FileLogService = unauthRetrofit.create(FileLogService::class.java)
}