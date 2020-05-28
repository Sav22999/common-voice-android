package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import okhttp3.RequestBody.Companion.toRequestBody
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.responses.RecordingResult
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Recording
import retrofit2.Response
import java.net.URLEncoder
import java.sql.Timestamp

class RecordingsRepository(
    database: AppDB,
    retrofitFactory: RetrofitFactory
) {

    private val recordingsDao = database.recordings()
    private val recordingsClient = retrofitFactory.makeRecordingService()

    suspend fun postRecording(recording: Recording): Response<RecordingResult> {
        val encodedSentence = URLEncoder.encode(recording.sentenceText, "UTF-8").replace("+", "%20")
        val requestBody = recording.audio.toRequestBody()
        return recordingsClient.sendRecording(encodedSentence, recording.sentenceId, requestBody)
    }

    @WorkerThread
    suspend fun insertRecording(recording: Recording) = recordingsDao.insertRecording(recording)

    @WorkerThread
    suspend fun deleteRecording(recording: Recording) = recordingsDao.deleteRecording(recording)

    @WorkerThread
    suspend fun getRecordingsCount() = recordingsDao.getCount()

    @WorkerThread
    suspend fun getOldRecordings(dateOfToday: Timestamp) = recordingsDao.getOldRecordings(dateOfToday.time)

    @WorkerThread
    suspend fun deleteOldRecordings(dateOfToday: Timestamp) = recordingsDao.deleteOldRecordings(dateOfToday.time)

    @WorkerThread
    suspend fun getAllRecordings() = recordingsDao.getAllRecordings()

}