package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.repositories.ReportsRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus

class ReportsUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.build(appContext)
    private val prefManager = MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val reportsRepository = ReportsRepository(db, retrofitFactory)

    override suspend fun doWork(): Result {
        reportsRepository.deleteOldReports(getTimestampOfNowPlus(seconds = 0))

        if (reportsRepository.getReportsCount() == 0) {
            db.close()
            return Result.success()
        }

        val availableReports = reportsRepository.getAllReports()

        availableReports.forEach { report ->
            reportsRepository.postReport(report)
            reportsRepository.deleteReport(report)
        }

        db.close()
        return Result.success()
    }

    companion object {

        private const val TAG = "reportsUploadWorker"

        private val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private val request = OneTimeWorkRequestBuilder<ReportsUploadWorker>()
            .setConstraints(constraint)
            .build()

        fun attachToWorkManager(wm: WorkManager) {
            wm.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                request
            )
        }

    }

}