package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.repositories.FailedRecordingsRepository
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.utils.PrefManager
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus

class RecordingsUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.build(appContext)
    private val prefManager = PrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val recordingsRepository = RecordingsRepository(db, retrofitFactory)
    private val failedRecordingsRepository = FailedRecordingsRepository(db)

    override suspend fun doWork(): Result {
        recordingsRepository.deleteOldRecordings(getTimestampOfNowPlus(seconds = 0))

        if (recordingsRepository.getRecordingsCount() == 0) {
            return Result.success()
        }

        val availableRecordings = recordingsRepository.getAllRecordings()

        availableRecordings.forEach { recording ->
            val result = sendRecording(recording)
            recordingsRepository.deleteRecording(recording)
            if (!result) {
                failedRecordingsRepository.insertFailedRecording(recording.toFailedRecording())
            }
        }

        if (failedRecordingsRepository.getNonCriticalFailedRecordingsCount() == 0) {
            return Result.success()
        }

        val failedRecordings = failedRecordingsRepository.getAllNonCriticalFailedRecordings()

        failedRecordings.forEach { failedRecording ->
            val recording = failedRecording.toRecording()
            val result = sendRecording(recording)
            if (result) {
                failedRecordingsRepository.deleteRecording(failedRecording)
            } else {
                failedRecording.failedPostsNumber++
                failedRecordingsRepository.updateFailedRecording(failedRecording)
            }
        }

        return if (failedRecordingsRepository.getNonCriticalFailedRecordingsCount() == 0) {
            Result.success()
        } else {
            Result.retry()
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

        private fun getRequest() = OneTimeWorkRequestBuilder<RecordingsUploadWorker>()
            .setConstraints(constraint)
            .build()

        fun WorkManager.enqueueRecordingsUploadWorker() {
            this.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                getRequest()
            )
        }

    }

}