package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.repositories.AppActionsRepository
import org.commonvoice.saverio_lib.repositories.StatsRepository

class AppUsageUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.getNewInstance(appContext)
    private val prefManager = MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val statsRepository = StatsRepository(prefManager, retrofitFactory)
    private val appActionsRepository = AppActionsRepository(db, prefManager, null, null)

    override suspend fun doWork(): Result = coroutineScope {
        return@coroutineScope try {

            appActionsRepository.getAllActions().forEach { action ->
                statsRepository.postAppUsageStatistics(
                    prefManager.appVersionCode,
                    prefManager.appSourceStore,
                    action
                ).let { result ->
                    if (result) {
                        appActionsRepository.deleteAction(action)
                    }
                }
            }

            Result.success()
        } finally {
            db.close()
        }
    }

    companion object {

        private const val TAG = "appUsageUploadWorker"

        fun attachToWorkManager(wm: WorkManager) {
            wm.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                WorkerUtil.genericOneTimeRequest<AppUsageUploadWorker>()
            )
        }

    }

}