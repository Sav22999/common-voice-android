package org.commonvoice.saverio_lib.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import org.commonvoice.saverio_lib.models.Clip

@Dao
interface ClipsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClip(clip: Clip)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClips(clips: List<Clip>)

    @Delete
    suspend fun deleteClip(clip: Clip)

    @Query("SELECT COUNT(clip_id) FROM clips")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(clip_id) FROM clips")
    fun getLiveCount(): LiveData<Int>

    @Query("SELECT * FROM clips WHERE lang IS :language ORDER BY expiry ASC LIMIT 1")
    suspend fun getOldestClip(language: String): Clip?

    @Query("DELETE FROM clips WHERE expiry <= :dateOfToday")
    suspend fun deleteOldClips(dateOfToday: Long)

    @Query("DELETE FROM clips WHERE lang IS NOT :language")
    suspend fun deleteWrongClips(language: String)

}