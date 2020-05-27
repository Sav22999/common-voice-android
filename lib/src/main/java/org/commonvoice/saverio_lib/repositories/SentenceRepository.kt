package org.commonvoice.saverio_lib.repositories

import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Sentence
import java.sql.Timestamp

class SentenceRepository(database: AppDB, retrofitFactory: RetrofitFactory) {

    private val sentenceDao = database.sentences()
    private val sentenceClient = retrofitFactory.makeSentenceService()

    suspend fun getNewSentences(count: Int) = sentenceClient.getSentence(count)

    suspend fun insertSentence(sentence: Sentence) = sentenceDao.insertSentence(sentence)

    suspend fun insertSentences(sentences: List<Sentence>) = sentenceDao.insertSentences(sentences)

    suspend fun deleteSentence(sentence: Sentence) = sentenceDao.deleteSentence(sentence)

    suspend fun getSentenceCount() = sentenceDao.getCount()

    fun getLiveSentenceCount() = sentenceDao.getLiveCount()

    suspend fun getOldestSentence() = sentenceDao.getOldestSentence()

    suspend fun getOldSentences(dateOfToday: Timestamp) = sentenceDao.getOldSentences(dateOfToday.time)

    suspend fun deleteOldSentences(dateOfToday: Timestamp) = sentenceDao.deleteOldSentences(dateOfToday.time)

}