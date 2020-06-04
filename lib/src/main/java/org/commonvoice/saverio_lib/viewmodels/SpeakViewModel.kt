package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import android.util.Log
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
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.repositories.ReportsRepository

class SpeakViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sentencesRepository: SentencesRepository,
    private val recordingsRepository: RecordingsRepository,
    private val mediaRecorderRepository: MediaRecorderRepository,
    private val mediaPlayerRepository: MediaPlayerRepository,
    private val recordingSoundIndicatorRepository: RecordingSoundIndicatorRepository,
    private val reportsRepository: ReportsRepository,
    private val workManager: WorkManager,
    private val mainPrefManager: MainPrefManager,
    private val speakPrefManager: SpeakPrefManager,
    private val statsPrefManager: StatsPrefManager
) : ViewModel() {

    private val _state: MutableLiveData<State> = savedStateHandle.getLiveData("state", State.STANDBY)
    val state: LiveData<State> get() = _state

    val hasReachedGoal = MutableLiveData(false)

    private val _currentSentence: MutableLiveData<Sentence> = savedStateHandle.getLiveData("sentence")
    val currentSentence: LiveData<Sentence> get() = _currentSentence

    private var currentRecording: Recording?
        get() = savedStateHandle["currentRecording"]
        set(value) { savedStateHandle["currentRecording"] = value }

    init {
        mediaRecorderRepository.setupRecorder()
    }

    fun startRecording() {
       _state.postValue(State.RECORDING)

        if (speakPrefManager.playRecordingSoundIndicator) {
            recordingSoundIndicatorRepository.playStartedSound {
                mediaRecorderRepository.startRecording()
            }
        } else {
            mediaRecorderRepository.startRecording()
        }
    }

    fun getDailyGoal() = statsPrefManager.dailyGoal

    fun stopRecording() {
        _currentSentence.value?.let { sentence ->
            mediaRecorderRepository.stopRecordingAndReadData(sentence, onError = {

                _state.postValue(State.RECORDING_ERROR)

            }, onSuccess = { recording ->

                currentRecording = recording

                if (speakPrefManager.skipRecordingConfirmation) {
                    _state.postValue(State.LISTENED)
                } else {
                    _state.postValue(State.RECORDED)
                }

                if (speakPrefManager.playRecordingSoundIndicator) {
                    recordingSoundIndicatorRepository.playFinishedSound()
                }

            })
        }
    }

    fun skipSentence() = viewModelScope.launch(Dispatchers.IO) {
        currentSentence.value?.let {
            sentencesRepository.deleteSentence(it)
        }
        withContext(Dispatchers.Main) {
           _state.postValue(State.STANDBY)
            SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
        }
    }

    fun startListening() {
        currentRecording?.let { recording ->
            mediaPlayerRepository.setup {
               _state.postValue(State.LISTENED)
            }
            mediaPlayerRepository.playRecording(recording)
           _state.postValue(State.LISTENING)
        }
    }

    fun stopListening() {
        mediaPlayerRepository.stopPlaying()
       _state.postValue(State.RECORDED)
    }

    fun redoRecording() {
       _state.postValue(State.RECORDING)
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
               _state.postValue(State.STANDBY)
                RecordingsUploadWorker.attachToWorkManager(workManager)
                SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
            }

            if (mainPrefManager.sessIdCookie != null) { //Logged
                statsPrefManager.todayRecorded++
                if (statsPrefManager.dailyGoal.checkDailyGoal()) {
                    hasReachedGoal.postValue(true)
                } else {
                    hasReachedGoal.postValue(false)
                }
            }
        }
    }

    fun loadNewSentence() = viewModelScope.launch(Dispatchers.IO) {
        val sentence = sentencesRepository.getOldestSentence()
        if (sentence != null) {
            _currentSentence.postValue(sentence)
        } else {
           _state.postValue(State.STANDBY)
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
            RECORDING_ERROR,
        }
    }

}