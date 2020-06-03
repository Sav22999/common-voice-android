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
import org.commonvoice.saverio_lib.mediaPlayer.RecordingSoundIndicatorRepository
import org.commonvoice.saverio_lib.mediaRecorder.MediaRecorderRepository
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.models.Report
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.repositories.ReportsRepository
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus

class SpeakViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sentencesRepository: SentencesRepository,
    private val recordingsRepository: RecordingsRepository,
    private val mediaRecorderRepository: MediaRecorderRepository,
    private val mediaPlayerRepository: MediaPlayerRepository,
    private val recordingSoundIndicatorRepository: RecordingSoundIndicatorRepository,
    private val reportsRepository: ReportsRepository,
    private val workManager: WorkManager,
    private val speakPrefManager: SpeakPrefManager
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
        state.postValue(State.RECORDING)

        if (speakPrefManager.playRecordingSoundIndicator) {
            recordingSoundIndicatorRepository.playStartedSound {
                mediaRecorderRepository.startRecording()
            }
        } else {
            mediaRecorderRepository.startRecording()
        }
    }

    fun stopRecording() {
        _currentSentence.value?.let { sentence ->
            currentRecording = mediaRecorderRepository.stopRecordingAndReadData(sentence)

            if (speakPrefManager.skipRecordingConfirmation) {
                state.postValue(State.LISTENED)
            } else {
                state.postValue(State.RECORDED)
            }

            if (speakPrefManager.playRecordingSoundIndicator) {
                recordingSoundIndicatorRepository.playFinishedSound()
            }
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

    fun stop() {
        when(state.value) {
            State.RECORDING -> mediaRecorderRepository.stop()
            State.LISTENING -> mediaPlayerRepository.stopPlaying()
        }

        currentRecording = null

        mediaRecorderRepository.clean()
        mediaPlayerRepository.clean()
        recordingSoundIndicatorRepository.clean()
    }

    override fun onCleared() {
        mediaRecorderRepository.clean()
        mediaPlayerRepository.clean()
        recordingSoundIndicatorRepository.clean()
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