package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.repositories.SentencesRepository
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.util.concurrent.TimeUnit

class SentencesDownloadWorker(
    appContext: Context,
    private val workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.build(appContext)
    private val prefManager =
        MainPrefManager(appContext)
    private val speakPrefManager =
        SpeakPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val requiredSentences: Int = speakPrefManager.requiredSentencesCount

    private val currentLanguage = prefManager.language

    private val sentenceRepository = SentencesRepository(db, retrofitFactory)

    override suspend fun doWork(): Result {

        sentenceRepository.deleteOldSentences(getTimestampOfNowPlus(seconds = 0))

        val numberDifference = requiredSentences - sentenceRepository.getSentenceCount()

        return when {
            numberDifference < 0 -> {
                db.close()
                Result.failure()
            }
            numberDifference == 0 -> {
                db.close()
                Result.success()
            }
            else -> {
                val newSentences = sentenceRepository.getNewSentences(numberDifference)
                if (!newSentences.isSuccessful) {
                    db.close()

                    if (workerParams.runAttemptCount > 5) {
                        Result.failure()
                    } else {
                        Result.retry()
                    }
                } else {
                    newSentences.body()?.let { sentences ->
                        sentenceRepository.insertSentences(sentences.map { it.setLanguage(currentLanguage) })
                    }

                    db.close()
                    Result.success()
                }
            }
        }

    }

    companion object {

        private const val TAG = "sentencesDownloadWorker"

        private val periodicWorkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(true)
            .setRequiresDeviceIdle(true)
            .build()

        private val periodicWorkRequest = PeriodicWorkRequestBuilder<SentencesDownloadWorker>(
            1, TimeUnit.DAYS, 8, TimeUnit.HOURS
        ).setConstraints(periodicWorkConstraint).build()

        private val oneTimeWorkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SentencesDownloadWorker>()
            .setConstraints(oneTimeWorkConstraints)
            .build()

        fun attachOneTimeJobToWorkManager(wm: WorkManager) {
            wm.enqueueUniqueWork(
                "oneTimeSentencesDownloadWorker",
                ExistingWorkPolicy.KEEP,
                oneTimeWorkRequest
            )
        }

        fun attachPeriodicJobToWorkManager(wm: WorkManager) {
            wm.enqueueUniquePeriodicWork(
                "periodicSentencesDownloadWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }

    }

}