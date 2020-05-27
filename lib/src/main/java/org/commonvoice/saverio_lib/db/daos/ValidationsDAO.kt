package org.commonvoice.saverio_lib.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.commonvoice.saverio_lib.models.Validation

@Dao
interface ValidationsDAO {

    @Insert
    suspend fun insertValidation(validation: Validation)

    @Delete
    suspend fun deleteValidation(validation: Validation)

    @Query("SELECT COUNT(id) FROM validations")
    suspend fun getCount(): Int

    @Query("SELECT * FROM validations WHERE expiry <= :dateOfToday")
    suspend fun getOldValidations(dateOfToday: Long): List<Validation>

    @Query("DELETE FROM validations WHERE expiry <= :dateOfToday")
    suspend fun deleteOldValidations(dateOfToday: Long)

    @Query("SELECT * FROM validations ORDER BY expiry ASC")
    fun getAllValidations(): LiveData<List<Validation>>

}