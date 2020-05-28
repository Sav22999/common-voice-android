package org.commonvoice.saverio_lib.db.daos

import androidx.room.*
import org.commonvoice.saverio_lib.models.FailedRecording
import org.commonvoice.saverio_lib.models.Recording

@Dao
interface FailedRecordingsDAO {

    @Insert
    suspend fun insertFailedRecording(recording: FailedRecording)

    @Delete
    suspend fun deleteFailedRecording(recording: FailedRecording)

    @Update
    suspend fun updateFailedRecording(recording: FailedRecording)

    @Query("SELECT COUNT(id) FROM failedRecordings")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(id) FROM failedRecordings WHERE tries < 5")
    suspend fun getNonCriticalCount(): Int

    @Query("SELECT * FROM failedRecordings WHERE expiry <= :dateOfToday")
    suspend fun getOldRecordings(dateOfToday: Long): List<FailedRecording>

    @Query("DELETE FROM failedRecordings WHERE expiry <= :dateOfToday")
    suspend fun deleteOldRecordings(dateOfToday: Long)

    @Query("SELECT * FROM failedRecordings ORDER BY expiry ASC")
    suspend fun getAllRecordings(): List<FailedRecording>

    @Query("SELECT * FROM failedRecordings WHERE tries < 5 ORDER BY expiry ASC")
    suspend fun getAllNonCriticalRecordings(): List<FailedRecording>

    @Query("DELETE FROM failedRecordings")
    suspend fun deleteAll()

}