package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.repositories.ClipRepository
import org.commonvoice.saverio_lib.api.responses.RecordingSent
import org.commonvoice.saverio_lib.models.RecordableSentence
import org.commonvoice.saverio_lib.repositories.SentenceRepository
import org.commonvoice.saverio_lib.repositories.SoundListeningRepository
import org.commonvoice.saverio_lib.repositories.SoundRecordingRepository

class SpeakViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val clipRepository: ClipRepository,
    private val sentenceRepository: SentenceRepository,
    private val recordingRepository: SoundRecordingRepository,
    private val listeningRepository: SoundListeningRepository
) : ViewModel() {

    var state: MutableLiveData<State> = savedStateHandle.getLiveData("state", State.STANDBY)

    var currentRecording: RecordableSentence
        get() = savedStateHandle.get("currentRecording") ?: RecordableSentence("", "")
        set(value) { savedStateHandle.set("currentRecording", value) }

    init {
        listeningRepository.setup {
            state.postValue(State.LISTENED)
        }
    }

    private fun resetListeningRepository() {
        listeningRepository.reset()
        listeningRepository.setup {
            state.postValue(State.LISTENED)
        }
    }

    fun startRecording() {
        recordingRepository.startRecording(currentRecording)
        state.postValue(State.RECORDING)
    }

    fun stopRecording() {
        recordingRepository.stopRecording()
        state.postValue(State.RECORDED)
    }

    fun startListening() {
        resetListeningRepository()
        listeningRepository.playRecording(currentRecording)
        state.postValue(State.LISTENING)
    }

    fun stopListening() {
        listeningRepository.stopPlaying()
        state.postValue(State.RECORDED)
    }

    fun sendRecording(): LiveData<Boolean> = liveData(Dispatchers.IO) {
        val copyOfRecording = currentRecording.copy()
        state.postValue(State.STANDBY)
        val response = clipRepository.sendRecording(copyOfRecording.sentence, copyOfRecording.sentence_id, copyOfRecording.file)
        emit(response.isSuccessful)
        resetListeningRepository()
    }

    fun redoRecording() {
        state.postValue(State.RECORDING)
        recordingRepository.redoRecording(currentRecording)
    }

    fun getSentence(): LiveData<RecordableSentence> = liveData(Dispatchers.IO) {
        val response = sentenceRepository.getSentences()
        response.body()?.let { emit(it.first()) }
    }

    fun reportSentence(): LiveData<Boolean> = liveData(Dispatchers.IO) {

    }

    override fun onCleared() {
        recordingRepository.clean()
        super.onCleared()
    }

    companion object {
        @Parcelize
        enum class State : Parcelable {
            STANDBY,
            RECORDING,
            RECORDED,
            LISTENING,
            LISTENED,
        }
    }

}