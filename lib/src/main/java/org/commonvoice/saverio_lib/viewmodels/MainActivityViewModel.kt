package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.repositories.SentencesRepository
import org.commonvoice.saverio_lib.utils.PrefManager
import java.sql.Timestamp

class MainActivityViewModel(
    private val prefManager: PrefManager,
    private val sentencesRepository: SentencesRepository
): ViewModel() {

    fun refreshLocalDatabase() = viewModelScope.launch(Dispatchers.IO) {
        sentencesRepository.deleteOldSentences(Timestamp(System.currentTimeMillis()))
        val sentenceCount = sentencesRepository.getSentenceCount()
        if (sentenceCount < prefManager.requiredSentencesCount) {
            val requiredSentences = prefManager.requiredSentencesCount - sentenceCount
            val newSentencesResponse = sentencesRepository.getNewSentences(requiredSentences)
            newSentencesResponse.let {
                if (it.isSuccessful && it.body() != null) {
                    val body = it.body()!!
                    sentencesRepository.insertSentences(body)
                }
            }
        }
    }

}