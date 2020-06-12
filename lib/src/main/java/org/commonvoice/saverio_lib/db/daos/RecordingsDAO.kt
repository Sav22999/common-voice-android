package org.commonvoice.saverio_lib.db.daos

import androidx.room.*
import org.commonvoice.saverio_lib.models.Recording

@Dao
interface RecordingsDAO {

    @Insert
    suspend fun insertRecording(recording: Recording)

    @Update
    suspend fun updateRecording(recording: Recording)

    @Delete
    suspend fun deleteRecording(recording: Recording)

    @Query("SELECT COUNT(id) FROM recordings")
    suspend fun getCount(): Int

    @Query("SELECT * FROM recordings WHERE expiry <= :dateOfToday")
    suspend fun getOldRecordings(dateOfToday: Long): List<Recording>

    @Query("DELETE FROM recordings WHERE expiry <= :dateOfToday")
    suspend fun deleteOldRecordings(dateOfToday: Long)

    @Query("SELECT * FROM recordings WHERE attempts <= 10 ORDER BY attempts ASC, expiry ASC")
    suspend fun getAllRecordings(): List<Recording>

    @Query("SELECT * FROM recordings WHERE attempts > 10")
    suspend fun geFailedRecordings(): List<Recording>

    @Query("DELETE FROM recordings WHERE attempts > 10")
    suspend fun deleteFailedRecordings()

}