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

    private val db = AppDB.getNewInstance(appContext)
    private val prefManager = MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val recordingsRepository = RecordingsRepository(db, retrofitFactory)

    private val speakPrefManager = SpeakPrefManager(appContext)

    override suspend fun doWork(): Result = coroutineScope {
        return@coroutineScope try {

            if (!speakPrefManager.saveRecordingsOnDevice) return@coroutineScope Result.success()

            val availableRecordings = recordingsRepository.getAllRecordings()

            val saveDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .openDirectory(speakPrefManager.deviceRecordingsLocation.drop(1).dropLast(1))

            availableRecordings.forEach { recording ->
                val mp3Filename = recording.sentenceId + '_' + System.currentTimeMillis()
                val csvFilename = "output"

                val fileMp3 = saveDirectory.openFile(mp3Filename + ".mp3")
                fileMp3.writeBytes(recording.audio)
                
                val fileCsv = saveDirectory.openFile(csvFilename + ".csv")
                fileCsv.write(Paths.get(path), (mp3Filename + "\t" +recording.sentenceText).toByteArray(), StandardOpenOption.APPEND)                
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            db.close()
        }
    }

}