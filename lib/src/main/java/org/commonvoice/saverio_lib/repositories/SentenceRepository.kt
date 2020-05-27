package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Sentence
import java.sql.Timestamp

class SentenceRepository(database: AppDB, retrofitFactory: RetrofitFactory) {

    private val sentenceDao = database.sentences()

    @WorkerThread
    suspend fun insertSentence(sentence: Sentence) = sentenceDao.insertSentence(sentence)

    @WorkerThread
    suspend fun insertSentences(sentences: List<Sentence>) = sentenceDao.insertSentences(sentences)

    @WorkerThread
    suspend fun deleteSentence(sentence: Sentence) = sentenceDao.deleteSentence(sentence)

    @WorkerThread
    suspend fun getSentenceCount() = sentenceDao.getCount()

    fun getLiveSentenceCount() = sentenceDao.getLiveCount()

    @WorkerThread
    suspend fun getOldestSentence() = sentenceDao.getOldestSentence()

    @WorkerThread
    suspend fun getOldSentences(dateOfToday: Timestamp) = sentenceDao.getOldSentences(dateOfToday.time)

    @WorkerThread
    suspend fun deleteOldSentences(dateOfToday: Timestamp) = sentenceDao.deleteOldSentences(dateOfToday.time)

}