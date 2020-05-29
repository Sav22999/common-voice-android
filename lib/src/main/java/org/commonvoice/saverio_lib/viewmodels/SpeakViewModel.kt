package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import androidx.lifecycle.*
import androidx.work.WorkManager
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.background.RecordingsUploadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.models.Sentence
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.repositories.SentencesRepository
import org.commonvoice.saverio_lib.mediaPlayer.MediaPlayerRepository
import org.commonvoice.saverio_lib.mediaRecorder.MediaRecorderRepository
import org.commonvoice.saverio_lib.models.Recording

class SpeakViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sentencesRepository: SentencesRepository,
    private val recordingsRepository: RecordingsRepository,
    private val mediaRecorderRepository: MediaRecorderRepository,
    private val mediaPlayerRepository: MediaPlayerRepository,
    private val workManager: WorkManager
) : ViewModel() {

    var state: MutableLiveData<State> = savedStateHandle.getLiveData("state", State.STANDBY)

    private val _currentSentence: MutableLiveData<Sentence> = savedStateHandle.getLiveData("sentence")
    val currentSentence: LiveData<Sentence> get() = _currentSentence

    private var currentRecording: Recording?
        get() = savedStateHandle["currentRecording"]
        set(value) { savedStateHandle["currentRecording"] = value }

    init {
        mediaRecorderRepository.setupRecorder()
    }

    fun startRecording() {
        mediaRecorderRepository.startRecording()
        state.postValue(State.RECORDING)
    }

    fun stopRecording() {
        _currentSentence.value?.let { sentence ->
            currentRecording = mediaRecorderRepository.stopRecordingAndReadData(sentence)
            state.postValue(State.RECORDED)
        }
    }

    fun skipSentence() = viewModelScope.launch(Dispatchers.IO) {
        currentSentence.value?.let {
            sentencesRepository.deleteSentence(it)
        }
        withContext(Dispatchers.Main) {
            state.postValue(State.STANDBY)
            SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
        }
    }

    fun startListening() {
        currentRecording?.let { recording ->
            mediaPlayerRepository.setup {
                state.postValue(State.LISTENED)
            }
            mediaPlayerRepository.playRecording(recording)
            state.postValue(State.LISTENING)
        }
    }

    fun stopListening() {
        mediaPlayerRepository.stopPlaying()
        state.postValue(State.RECORDED)
    }

    fun redoRecording() {
        state.postValue(State.RECORDING)
        mediaRecorderRepository.redoRecording()
    }

    fun sendRecording() = viewModelScope.launch(Dispatchers.IO) {
        currentRecording?.let { recording ->
            recordingsRepository.insertRecording(recording)
            currentSentence.value?.let {
                sentencesRepository.deleteSentence(it)
            }
            currentRecording = null
            withContext(Dispatchers.Main) {
                state.postValue(State.STANDBY)
                RecordingsUploadWorker.attachToWorkManager(workManager)
                SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
            }
        }
    }

    fun loadNewSentence() = viewModelScope.launch(Dispatchers.IO) {
        val sentence = sentencesRepository.getOldestSentence()
        if (sentence != null) {
            _currentSentence.postValue(sentence)
        } else {
            state.postValue(State.STANDBY)
        }
    }

    override fun onCleared() {
        mediaRecorderRepository.clean()
        mediaPlayerRepository.clean()
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