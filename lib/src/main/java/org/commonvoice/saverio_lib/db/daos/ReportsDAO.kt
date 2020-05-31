package org.commonvoice.saverio_lib.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.commonvoice.saverio_lib.models.Report

@Dao
interface ReportsDAO {

    @Insert
    suspend fun insertReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)

    @Query("SELECT * FROM reports")
    suspend fun getAllReports(): List<Report>

}