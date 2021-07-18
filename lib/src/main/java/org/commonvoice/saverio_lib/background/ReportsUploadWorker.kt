package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.repositories.ReportsRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus

class ReportsUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.getNewInstance(appContext)
    private val prefManager = MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val reportsRepository = ReportsRepository(db, retrofitFactory)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            reportsRepository.deleteOldReports(getTimestampOfNowPlus(seconds = 0))

            if (reportsRepository.getReportsCount() == 0) {
                db.close()
                return@coroutineScope Result.success()
            }

            val availableReports = reportsRepository.getAllReports()

            availableReports.forEach { report ->
                reportsRepository.postReport(report)
                reportsRepository.deleteReport(report)
            }

            return@coroutineScope Result.success()
        } finally {
            db.close()
        }
    }

    companion object {

        private const val TAG = "reportsUploadWorker"

        fun attachToWorkManager(
            wm: WorkManager,
            wifiOnly: Boolean = false
        ) {
            wm.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                WorkerUtil.request<ReportsUploadWorker>(wifiOnly)
            )
        }

    }

}