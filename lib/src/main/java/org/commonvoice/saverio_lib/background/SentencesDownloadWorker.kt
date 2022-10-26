package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.repositories.SentencesRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import kotlin.math.log

class SentencesDownloadWorker(
    appContext: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.getNewInstance(appContext)
    private val prefManager =
        MainPrefManager(appContext)
    private val speakPrefManager =
        SpeakPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val requiredSentences: Int = speakPrefManager.requiredSentencesCount

    private val currentLanguage = prefManager.language

    private val sentenceRepository = SentencesRepository(db, retrofitFactory)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            sentenceRepository.deleteOldSentences(getTimestampOfNowPlus(seconds = 0))
            sentenceRepository.deleteWrongSentences(currentLanguage)

            val numberDifference = requiredSentences - sentenceRepository.getSentenceCount()
            var numberDifferenceToUse = numberDifference
            if (numberDifferenceToUse > 50) numberDifferenceToUse = 50

            return@coroutineScope when {

                numberDifference < 0 -> {
                    Result.failure()
                }
                numberDifference == 0 -> {
                    Result.success()
                }
                else -> {
                    val newSentences = sentenceRepository.getNewSentences(numberDifferenceToUse)
                    if (!newSentences.isSuccessful) {

                        if (workerParams.runAttemptCount > 5) {
                            Result.failure()
                        } else {
                            Result.retry()
                        }
                    } else {
                        newSentences.body()?.let { sentences ->
                            sentenceRepository.insertSentences(sentences.map {
                                it.setLanguage(
                                    currentLanguage
                                )
                            })
                        }

                        speakPrefManager.noMoreSentencesAvailable =
                            newSentences.body()?.isEmpty() ?: false

                        Result.success()
                    }
                }
            }
        } finally {
            sentenceRepository.deleteWrongSentences(currentLanguage)
            db.close()
        }
    }

    companion object {

        fun attachOneTimeJobToWorkManager(
            wm: WorkManager,
            workPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP,
            wifiOnly: Boolean = false
        ) {
            wm.enqueueUniqueWork(
                "oneTimeSentencesDownloadWorker",
                workPolicy,
                WorkerUtil.request<SentencesDownloadWorker>(wifiOnly)
            )
        }

    }

}