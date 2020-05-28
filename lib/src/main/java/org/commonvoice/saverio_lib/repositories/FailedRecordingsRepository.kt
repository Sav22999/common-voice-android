package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.FailedRecording
import java.sql.Timestamp

@WorkerThread
class FailedRecordingsRepository(
    database: AppDB
) {

    private val failedRecordingsDAO = database.failedRecordings()

    suspend fun insertFailedRecording(recording: FailedRecording) = failedRecordingsDAO.insertFailedRecording(recording)

    suspend fun deleteRecording(recording: FailedRecording) = failedRecordingsDAO.deleteFailedRecording(recording)

    suspend fun updateFailedRecording(recording: FailedRecording) = failedRecordingsDAO.updateFailedRecording(recording)

    suspend fun getFailedRecordingsCount() = failedRecordingsDAO.getCount()

    suspend fun getNonCriticalFailedRecordingsCount() = failedRecordingsDAO.getNonCriticalCount()

    suspend fun getOldFailedRecordings(dateOfToday: Timestamp) = failedRecordingsDAO.getOldRecordings(dateOfToday.time)

    suspend fun deleteOldFailedRecordings(dateOfToday: Timestamp) = failedRecordingsDAO.deleteOldRecordings(dateOfToday.time)

    suspend fun getAllFailedRecordings() = failedRecordingsDAO.getAllRecordings()

    suspend fun getAllNonCriticalFailedRecordings() = failedRecordingsDAO.getAllNonCriticalRecordings()

    suspend fun deleteAllFailedRecordings() = failedRecordingsDAO.deleteAll()

}