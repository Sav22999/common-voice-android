package org.commonvoice.saverio_lib.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.repositories.SentenceRepository
import org.commonvoice.saverio_lib.api.responses.RecordingSent
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.repositories.SoundRecordingRepository
import java.io.File

class SpeakViewModel(
    private val sentenceRepository: SentenceRepository,
    private val recordingRepository: SoundRecordingRepository
) : ViewModel() {

    var isRecording = false

    fun startRecording(recording: Recording) {
        recordingRepository.startRecording(recording)
    }

    fun stopRecording() {
        recordingRepository.stopRecording()
    }

    fun sendRecording(recording: Recording): LiveData<RecordingSent> = liveData(Dispatchers.IO) {
        val response = sentenceRepository.sendRecording(recording.sentence, recording.sentence_id, recording.file)
            if (response.isSuccessful) {
                Log.i("SpeakViewModel", "Successfully sent a recording")
                response.body()?.let { emit(it) }
            } else {
                Log.e("SpeakViewModel", "An error occurred while sending a recording")
                Log.e("SpeakViewModel", response.errorBody().toString())
            }
        }

    override fun onCleared() {
        recordingRepository.clean()
        super.onCleared()
    }

}