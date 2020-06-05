package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.util.concurrent.TimeUnit

class RecordingsUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.build(appContext)
    private val prefManager =
        MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val recordingsRepository = RecordingsRepository(db, retrofitFactory)

    override suspend fun doWork(): Result {
        recordingsRepository.deleteOldRecordings(getTimestampOfNowPlus(seconds = 0))
        recordingsRepository.deleteFailedRecordings()

        if (recordingsRepository.getRecordingsCount() == 0) {
            db.close()
            return Result.success()
        }

        val availableRecordings = recordingsRepository.getAllRecordings()

        availableRecordings.forEach { recording ->
            val result = sendRecording(recording) //false
            if (result) {
                recordingsRepository.deleteRecording(recording)
            } else {
                recordingsRepository.updateRecording(recording.increaseAttempt())
            }
        }

        return if (recordingsRepository.getRecordingsCount() != 0) {
            db.close()
            Result.retry()
        } else {
            db.close()
            Result.success()
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
            wm.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                request
            )
        }

    }

}