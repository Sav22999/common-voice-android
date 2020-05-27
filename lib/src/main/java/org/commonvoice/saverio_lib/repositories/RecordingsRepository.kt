package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Recording
import java.sql.Timestamp

class RecordingsRepository(database: AppDB) {

    private val recordingsDao = database.recordings()

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

    fun getAllRecordings() = recordingsDao.getAllRecordings()

}