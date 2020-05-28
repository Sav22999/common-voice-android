package org.commonvoice.saverio_lib.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.commonvoice.saverio_lib.models.Sentence

@Dao
interface SentencesDAO {

    @Insert
    suspend fun insertSentence(sentence: Sentence)

    @Insert
    suspend fun insertSentences(sentences: List<Sentence>)

    @Delete
    suspend fun deleteSentence(sentence: Sentence)

    @Query("SELECT COUNT(id) FROM sentences")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(id) FROM sentences")
    fun getLiveCount(): LiveData<Int>

    @Query("SELECT * FROM sentences ORDER BY expiry ASC LIMIT 1")
    suspend fun getOldestSentence(): Sentence?

    @Query("SELECT * FROM sentences WHERE expiry <= :dateOfToday")
    suspend fun getOldSentences(dateOfToday: Long): List<Sentence>

    @Query("DELETE FROM sentences WHERE expiry <= :dateOfToday")
    suspend fun deleteOldSentences(dateOfToday: Long)

}