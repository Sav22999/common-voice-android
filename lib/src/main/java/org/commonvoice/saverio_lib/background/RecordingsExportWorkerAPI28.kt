package org.commonvoice.saverio_lib.background

import android.content.Context
import android.os.Environment
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.utils.openDirectory
import org.commonvoice.saverio_lib.utils.openFile

internal class RecordingsExportWorkerAPI28(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.getInstance(appContext)
    private val prefManager = MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val recordingsRepository = RecordingsRepository(db, retrofitFactory)

    private val speakPrefManager = SpeakPrefManager(appContext)

    override suspend fun doWork(): Result = coroutineScope {
        return@coroutineScope try {

            if (!speakPrefManager.saveRecordingsOnDevice) return@coroutineScope Result.success()

            val availableRecordings = recordingsRepository.getAllRecordings()

            val saveDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)

            if (!saveDirectory.exists())
                saveDirectory.mkdir()

            //drop and dropLast to remove initial and final /
            saveDirectory.openDirectory(
                speakPrefManager.deviceRecordingsLocation.drop(1).dropLast(1)
            )

            if (!saveDirectory.exists())
                saveDirectory.mkdir()

            availableRecordings.forEach { recording ->
                val file =
                    saveDirectory.openFile(recording.sentenceId + '_' + System.currentTimeMillis() + ".mp3")

                file.writeBytes(recording.audio)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {

        private const val TAG = "recordingsExportAPI28UploadWorker"

        private val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        internal val request = OneTimeWorkRequestBuilder<RecordingsExportWorkerAPI28>()
            .setConstraints(constraint)
            .build()

    }

}