package org.commonvoice.saverio_lib.background

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import java.io.FileOutputStream

@RequiresApi(29)
class RecordingsExportWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.getInstance(appContext)
    private val prefManager = MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val recordingsRepository = RecordingsRepository(db, retrofitFactory)

    private val speakPrefManager = SpeakPrefManager(appContext)

    private val resolver = appContext.contentResolver

    private val recordingCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Downloads.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Downloads.EXTERNAL_CONTENT_URI
    }

    override suspend fun doWork(): Result = coroutineScope {
        return@coroutineScope try {

            if (!speakPrefManager.saveRecordingsOnDevice) return@coroutineScope Result.success()

            val availableRecordings = recordingsRepository.getAllRecordings()

            availableRecordings.forEach { recording ->
                val recordingDetails = ContentValues().apply {
                    put(
                        MediaStore.Downloads.DISPLAY_NAME,
                        recording.sentenceId + '_' + System.currentTimeMillis()
                    )
                    put(
                        MediaStore.Downloads.MIME_TYPE,
                        "audio/mpeg"
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Downloads.IS_PENDING, 1)
                        put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS + speakPrefManager.deviceRecordingsLocation
                        )
                    }
                }

                val recordingContentUri = resolver.insert(recordingCollection, recordingDetails)
                    ?: return@coroutineScope Result.failure()

                resolver.openFileDescriptor(recordingContentUri, "w", null).use { fd ->
                    if (fd != null) {
                        FileOutputStream(fd.fileDescriptor).let {
                            it.write(recording.audio)
                            it.close()
                        }
                    } else {
                        return@coroutineScope Result.failure()
                    }

                    fd.close()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    recordingDetails.clear()
                    recordingDetails.put(MediaStore.Downloads.IS_PENDING, 0)
                    recordingDetails.put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + speakPrefManager.deviceRecordingsLocation
                    )
                    resolver.update(recordingContentUri, recordingDetails, null, null)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            db.close()
        }
    }

    companion object {

        private const val TAG = "recordingsExportUploadWorker"

        private val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        internal val request = OneTimeWorkRequestBuilder<RecordingsExportWorker>()
            .setConstraints(constraint)
            .build()

    }

}