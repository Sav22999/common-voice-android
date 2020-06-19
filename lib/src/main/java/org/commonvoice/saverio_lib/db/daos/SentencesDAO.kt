package org.commonvoice.saverio_lib.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import org.commonvoice.saverio_lib.models.Sentence

@Dao
interface SentencesDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSentence(sentence: Sentence)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSentences(sentences: List<Sentence>)

    @Delete
    suspend fun deleteSentence(sentence: Sentence)

    @Query("SELECT COUNT(id) FROM sentences")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(id) FROM sentences")
    fun getLiveCount(): LiveData<Int>

    @Query("SELECT * FROM sentences ORDER BY expiry ASC LIMIT 1")
    suspend fun getOldestSentence(): Sentence?

    @Query("DELETE FROM sentences WHERE expiry <= :dateOfToday")
    suspend fun deleteOldSentences(dateOfToday: Long)

    @Query("DELETE FROM sentences WHERE lang IS NOT :language")
    suspend fun deleteWrongSentences(language: String)

}