package org.commonvoice.saverio_lib.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.commonvoice.saverio_lib.models.AppAction

@Dao
interface AppActionsDAO {

    @Insert
    suspend fun insertAction(action: AppAction)

    @Delete
    suspend fun deleteAction(action: AppAction)

    @Query("SELECT * FROM appAction")
    suspend fun getAll(): List<AppAction>

}