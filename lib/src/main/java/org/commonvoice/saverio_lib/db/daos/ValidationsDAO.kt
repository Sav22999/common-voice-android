package org.commonvoice.saverio_lib.db.daos

import androidx.room.*
import org.commonvoice.saverio_lib.models.Validation

@Dao
interface ValidationsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertValidation(validation: Validation)

    @Update
    suspend fun updateValidation(validation: Validation)

    @Delete
    suspend fun deleteValidation(validation: Validation)

    @Query("SELECT COUNT(id) FROM validations")
    suspend fun getCount(): Int

    @Query("SELECT * FROM validations WHERE expiry <= :dateOfToday")
    suspend fun getOldValidations(dateOfToday: Long): List<Validation>

    @Query("DELETE FROM validations WHERE expiry <= :dateOfToday")
    suspend fun deleteOldValidations(dateOfToday: Long)

    @Query("SELECT * FROM validations ORDER BY attempts, expiry ASC")
    suspend fun getAllValidations(): List<Validation>

    @Query("SELECT * FROM validations WHERE attempts > 10")
    suspend fun getFailedValidations(): List<Validation>

    @Query("DELETE FROM validations WHERE attempts > 10")
    suspend fun deleteFailedValidations()

}