package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Validation
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.repositories.ValidationsRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import org.koin.core.KoinComponent
import org.koin.core.inject

class ValidationsUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams), KoinComponent {

    private val db = AppDB.getNewInstance(appContext)
    private val prefManager =
        MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val validationsRepository = ValidationsRepository(db, retrofitFactory)

    private val mainPrefManager = MainPrefManager(appContext)
    private val statsPrefManager by inject<StatsPrefManager>()

    override suspend fun doWork(): Result = coroutineScope {
        try {
            validationsRepository.deleteOldValidations(getTimestampOfNowPlus(seconds = 0))
            validationsRepository.deleteFailedValidations()

            if (validationsRepository.getValidationsCount() == 0) {
                return@coroutineScope Result.success()
            }

            val availableValidations = validationsRepository.getAllValidations()

            availableValidations.forEach { validation ->
                val result = sendValidation(validation)

                if (result) {
                    validationsRepository.deleteValidation(validation)
                    if (mainPrefManager.sessIdCookie != null) {
                        statsPrefManager.todayValidated++
                        statsPrefManager.localValidated++
                        statsPrefManager.localLevel++
                    }
                } else {
                    validationsRepository.updateValidation(validation.increaseAttempt())
                }
            }

            return@coroutineScope if (validationsRepository.getValidationsCount() != 0) {
                Result.retry()
            } else {
                Result.success()
            }
        } finally {
            db.close()
        }
    }

    private suspend fun sendValidation(validation: Validation): Boolean {
        val result = validationsRepository.postValidation(validation)
        return result.isSuccessful
    }

    companion object {

        private const val TAG = "validationsUploadWorker"

        fun attachToWorkManager(
            wm: WorkManager,
            wifiOnly: Boolean = false
        ) {
            wm.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.KEEP,
                WorkerUtil.request<ValidationsUploadWorker>(wifiOnly)
            )
        }

    }

}