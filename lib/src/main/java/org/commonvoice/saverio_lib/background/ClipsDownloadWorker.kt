package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.repositories.ClipsRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus

class ClipsDownloadWorker(
    appContext: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.getNewInstance(appContext)
    private val prefManager = MainPrefManager(appContext)
    private val listenPrefManager = ListenPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val requiredClips: Int = listenPrefManager.requiredClipsCount

    private val currentLanguage = prefManager.language

    private val clipsRepository = ClipsRepository(db, retrofitFactory)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            clipsRepository.deleteOldClips(getTimestampOfNowPlus(seconds = 0))
            clipsRepository.deleteWrongClips(currentLanguage)

            val numberDifference = requiredClips - clipsRepository.getClipsCount()
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
                    var result = Result.success()

                    listenPrefManager.noMoreClipsAvailable = false

                    val newClips =
                        clipsRepository.loadNewClips(numberDifferenceToUse, forEachClip = { clip ->
                            clipsRepository.insertClip(clip.also {
                                it.sentence.setLanguage(currentLanguage)
                            })
                        }, onError = {
                            result = if (workerParams.runAttemptCount > 5) {
                                Result.failure()
                            } else {
                                Result.retry()
                            }
                        }, onEmpty = {
                            listenPrefManager.noMoreClipsAvailable = true
                        })

                    result
                }
            }
        } finally {
            clipsRepository.deleteWrongClips(currentLanguage)
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
                "oneTimeClipsDownloadWorker",
                workPolicy,
                WorkerUtil.request<ClipsDownloadWorker>(wifiOnly)
            )
        }

    }

}