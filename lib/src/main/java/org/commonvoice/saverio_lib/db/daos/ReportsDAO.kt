package org.commonvoice.saverio_lib.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.models.Report

@Dao
interface ReportsDAO {

    @Insert
    suspend fun insertReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)

    @Query("SELECT * FROM reports")
    suspend fun getAllReports(): List<Report>

    @Query("SELECT COUNT(id) FROM reports")
    suspend fun getCount(): Int

    @Query("DELETE FROM reports WHERE expiry_date <= :dateOfToday")
    suspend fun deleteOldReports(dateOfToday: Long)

}