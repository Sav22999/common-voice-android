package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.repositories.SentenceRepository
import org.commonvoice.saverio_lib.utils.PrefManager
import java.sql.Timestamp

class MainActivityViewModel(
    private val prefManager: PrefManager,
    private val sentenceRepository: SentenceRepository
): ViewModel() {

    fun refreshLocalDatabase() = viewModelScope.launch(Dispatchers.IO) {
        sentenceRepository.deleteOldSentences(Timestamp(System.currentTimeMillis()))
        val sentenceCount = sentenceRepository.getSentenceCount()
        if (sentenceCount < prefManager.requiredSentencesCount) {
            val requiredSentences = prefManager.requiredSentencesCount - sentenceCount
            val newSentencesResponse = sentenceRepository.getNewSentences(requiredSentences)
            newSentencesResponse.let {
                if (it.isSuccessful && it.body() != null) {
                    val body = it.body()!!
                    sentenceRepository.insertSentences(body)
                }
            }
        }
    }

}