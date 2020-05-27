package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Validation
import java.sql.Timestamp

class ValidationsRepository(database: AppDB) {

    private val validationsDao = database.validations()

    @WorkerThread
    suspend fun insertValidation(validation: Validation) = validationsDao.insertValidation(validation)

    @WorkerThread
    suspend fun deleteValidation(validation: Validation) = validationsDao.deleteValidation(validation)

    @WorkerThread
    suspend fun getValidationsCount() = validationsDao.getCount()

    @WorkerThread
    suspend fun getOldValidation(dateOfToday: Timestamp) = validationsDao.getOldValidations(dateOfToday.time)

    @WorkerThread
    suspend fun deleteOldValidations(dateOfToday: Timestamp) = validationsDao.deleteOldValidations(dateOfToday.time)

    fun getAllValidations() = validationsDao.getAllValidations()

}