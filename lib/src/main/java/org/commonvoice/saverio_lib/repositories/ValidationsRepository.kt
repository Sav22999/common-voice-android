package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitValidationBody
import org.commonvoice.saverio_lib.api.responseBodies.RetrofitValidationResult
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Validation
import retrofit2.Response
import java.sql.Timestamp

class ValidationsRepository(
    database: AppDB,
    retrofitFactory: RetrofitFactory
) {

    private val validationsDao = database.validations()

    private val validationsClient = retrofitFactory.makeValidationsService()

    suspend fun postValidation(validation: Validation): Response<RetrofitValidationResult> {
        return validationsClient.sendValidation(
            validation.language,
            validation.id,
            RetrofitValidationBody.fromValidation(validation)
        )
    }

    @WorkerThread
    suspend fun insertValidation(validation: Validation) = validationsDao.insertValidation(validation)

    @WorkerThread
    suspend fun updateValidation(validation: Validation) = validationsDao.updateValidation(validation)

    @WorkerThread
    suspend fun deleteValidation(validation: Validation) = validationsDao.deleteValidation(validation)

    @WorkerThread
    suspend fun getValidationsCount() = validationsDao.getCount()

    @WorkerThread
    suspend fun getOldValidation(dateOfToday: Timestamp) = validationsDao.getOldValidations(dateOfToday.time)

    @WorkerThread
    suspend fun deleteOldValidations(dateOfToday: Timestamp) = validationsDao.deleteOldValidations(dateOfToday.time)

    @WorkerThread
    suspend fun getAllValidations() = validationsDao.getAllValidations()

    @WorkerThread
    suspend fun getFailedValidations() = validationsDao.getFailedValidations()

    @WorkerThread
    suspend fun deleteFailedValidations() = validationsDao.deleteFailedValidations()

}