package org.commonvoice.saverio_lib.background

import android.content.Context
import androidx.work.*
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Validation
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.repositories.ValidationsRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus
import java.util.concurrent.TimeUnit

class ValidationsUploadWorker(
    appContext: Context,
    private val workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    private val db = AppDB.build(appContext)
    private val prefManager =
        MainPrefManager(appContext)
    private val retrofitFactory = RetrofitFactory(prefManager)

    private val validationsRepository = ValidationsRepository(db, retrofitFactory)

    private val mainPrefManager = MainPrefManager(appContext)
    private val statsPrefManager = StatsPrefManager(appContext)

    override suspend fun doWork(): Result {
        validationsRepository.deleteOldValidations(getTimestampOfNowPlus(seconds = 0))
        validationsRepository.deleteFailedValidations()

        if (validationsRepository.getValidationsCount() == 0) {
            db.close()
            return Result.success()
        }

        val availableValidations = validationsRepository.getAllValidations()

        availableValidations.forEach { validation ->
            val result = sendValidation(validation)

            if (result) {
                validationsRepository.deleteValidation(validation)
                if (mainPrefManager.sessIdCookie != null) {
                    statsPrefManager.todayValidated++
                }
            } else {
                validationsRepository.updateValidation(validation.increaseAttempt())
            }
        }

        return if (validationsRepository.getValidationsCount() != 0) {
            db.close()
            Result.retry()
        } else {
            db.close()
            Result.success()
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