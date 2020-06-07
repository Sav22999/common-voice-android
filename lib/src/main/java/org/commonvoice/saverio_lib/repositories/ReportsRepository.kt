package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitReport
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Report
import java.sql.Timestamp

class ReportsRepository(
    database: AppDB,
    retrofitFactory: RetrofitFactory
) {

    private val reportsDao = database.reports()

    private val reportsClient = retrofitFactory.makeReportsService()

    suspend fun postReport(report: Report) {
        reportsClient.sendReport(
            RetrofitReport(
                report
            )
        )
    }

    @WorkerThread
    suspend fun insertReport(report: Report) = reportsDao.insertReport(report)

    @WorkerThread
    suspend fun deleteReport(report: Report) = reportsDao.deleteReport(report)

    @WorkerThread
    suspend fun getAllReports() = reportsDao.getAllReports()

    @WorkerThread
    suspend fun getReportsCount() = reportsDao.getCount()

    @WorkerThread
    suspend fun deleteOldReports(timeOfToday: Timestamp) = reportsDao.deleteOldReports(timeOfToday.time)

}