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
import java.util.concurrent.TimeUnit

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

        private val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private val request = OneTimeWorkRequestBuilder<ValidationsUploadWorker>()
            .setConstraints(constraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
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