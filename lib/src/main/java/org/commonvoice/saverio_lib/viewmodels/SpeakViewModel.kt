package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import androidx.lifecycle.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.models.Sentence
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.repositories.SentencesRepository
import org.commonvoice.saverio_lib.repositories.SoundListeningRepository
import org.commonvoice.saverio_lib.repositories.SoundRecordingRepository

class SpeakViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sentencesRepository: SentencesRepository,
    private val recordingsRepository: RecordingsRepository,
    private val recordingRepository: SoundRecordingRepository,
    private val soundListeningRepository: SoundListeningRepository
) : ViewModel() {

    var state: MutableLiveData<State> = savedStateHandle.getLiveData("state", State.STANDBY)

    private val _currentSentence: MutableLiveData<Sentence> = savedStateHandle.getLiveData("sentence")
    val currentSentence: LiveData<Sentence> get() = _currentSentence

    init {
        /*listeningRepository.setup {
            state.postValue(State.LISTENED)
        }*/
    }

    private fun resetListeningRepository() {
        /*listeningRepository.reset()
        listeningRepository.setup {
            state.postValue(State.LISTENED)
        }*/
    }

    fun startRecording() {
        /*recordingRepository.startRecording(currentRecording)
        state.postValue(State.RECORDING)*/
    }

    fun stopRecording() {
        /*recordingRepository.stopRecording()
        state.postValue(State.RECORDED)*/
    }

    fun startListening() {
        /*resetListeningRepository()
        listeningRepository.playRecording(currentRecording)
        state.postValue(State.LISTENING)*/
    }

    fun stopListening() {
        //listeningRepository.stopPlaying()
        state.postValue(State.RECORDED)
    }

    fun sendRecording(): LiveData<Boolean> = liveData(Dispatchers.IO) {
        /*val copyOfRecording = currentRecording.copy()
        state.postValue(State.STANDBY)
        val response = clipRepository.sendRecording(copyOfRecording.sentence, copyOfRecording.sentence_id, copyOfRecording.file)
        emit(response.isSuccessful)
        resetListeningRepository()*/
    }

    fun redoRecording() {
        /*state.postValue(State.RECORDING)
        recordingRepository.redoRecording(currentRecording)*/
    }

    fun loadNewSentence() = viewModelScope.launch(Dispatchers.IO) {
        val sentence = sentencesRepository.getOldestSentence()
        if (sentence != null) {
            _currentSentence.postValue(sentence)
        } else {
            state.postValue(State.STANDBY)
        }
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