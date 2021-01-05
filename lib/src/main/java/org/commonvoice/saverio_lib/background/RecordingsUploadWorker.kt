package org.commonvoice.saverio_lib.background

import android.content.Context
import android.os.Build
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class RecordingsUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val db = AppDB.getNewInstance(appContext)
    private val prefManager =
        MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val recordingsRepository = RecordingsRepository(db, retrofitFactory)

    private val mainPrefManager = MainPrefManager(appContext)
    private val statsPrefManager by inject<StatsPrefManager>()

    override suspend fun doWork(): Result = coroutineScope {
        try {
            recordingsRepository.deleteOldRecordings(getTimestampOfNowPlus(seconds = 0))
            recordingsRepository.deleteFailedRecordings()

            if (recordingsRepository.getRecordingsCount() == 0) {
                return@coroutineScope Result.success()
            }

            val availableRecordings = recordingsRepository.getAllRecordings()

            availableRecordings.forEach { recording ->
                val result = sendRecording(recording) //false

                if (result) {
                    recordingsRepository.deleteRecording(recording)
                    if (mainPrefManager.sessIdCookie != null) {
                        statsPrefManager.todayRecorded++
                        statsPrefManager.localRecorded++
                        statsPrefManager.localLevel++
                    }
                } else {
                    recordingsRepository.updateRecording(recording.increaseAttempt())
                }
            }

            return@coroutineScope if (recordingsRepository.getRecordingsCount() != 0) {
                Result.retry()
            } else {
                Result.success()
            }
        } finally {
            db.close()
        }
    }

    private suspend fun sendRecording(recording: Recording): Boolean {
        val response = recordingsRepository.postRecording(recording)
        return response.isSuccessful
    }

    companion object {

        private const val TAG = "recordingsUploadWorker"

        private val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private val request = OneTimeWorkRequestBuilder<RecordingsUploadWorker>()
            .setConstraints(constraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        fun attachToWorkManager(wm: WorkManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                wm.beginUniqueWork(
                    TAG,
                    ExistingWorkPolicy.KEEP,
                    RecordingsExportWorker.request
                ).then(request).enqueue()
            } else {
                wm.beginUniqueWork(
                    TAG,
                    ExistingWorkPolicy.KEEP,
                    RecordingsExportWorkerAPI28.request
                ).then(request).enqueue()
            }
        }

    }

}