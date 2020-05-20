package org.commonvoice.saverio_api.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import org.commonvoice.saverio_api.repositories.SentenceRepository
import org.commonvoice.saverio_api.responses.RecordingSent
import java.io.File

class SpeakViewModel(
    private val sentenceRepository: SentenceRepository
) : ViewModel() {

    fun sendRecording(sentence: String, id: String, file: File): LiveData<RecordingSent> =
        liveData(Dispatchers.IO) {
            val response = sentenceRepository.sendRecording(sentence, id, file)
            if (response.isSuccessful) {
                Log.i("SpeakViewModel", "Successfully sent a recording")
                Log.i("SpeakViewModel", response.body().toString())
                response.body()?.let { emit(it) }
            } else {
                Log.e("SpeakViewModel", "An error occurred while sending a recording")
                Log.e("SpeakViewModel", response.errorBody().toString())
            }
        }

}