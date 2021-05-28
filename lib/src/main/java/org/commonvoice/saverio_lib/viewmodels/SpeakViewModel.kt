package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import androidx.lifecycle.*
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.commonvoice.saverio_lib.background.RecordingsUploadWorker
import org.commonvoice.saverio_lib.background.ReportsUploadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.mediaPlayer.MediaPlayerRepository
import org.commonvoice.saverio_lib.mediaPlayer.RecordingSoundIndicatorRepository
import org.commonvoice.saverio_lib.mediaRecorder.MediaRecorderRepository
import org.commonvoice.saverio_lib.models.AppAction
import org.commonvoice.saverio_lib.models.Recording
import org.commonvoice.saverio_lib.models.Report
import org.commonvoice.saverio_lib.models.Sentence
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.repositories.AppActionsRepository
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.repositories.ReportsRepository
import org.commonvoice.saverio_lib.repositories.SentencesRepository

class SpeakViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sentencesRepository: SentencesRepository,
    private val recordingsRepository: RecordingsRepository,
    private val mediaRecorderRepository: MediaRecorderRepository,
    private val mediaPlayerRepository: MediaPlayerRepository,
    private val recordingSoundIndicatorRepository: RecordingSoundIndicatorRepository,
    private val reportsRepository: ReportsRepository,
    private val workManager: WorkManager,
    private val speakPrefManager: SpeakPrefManager,
    private val appActionsRepository: AppActionsRepository,
) : ViewModel() {

    private val _state: MutableLiveData<State> =
        savedStateHandle.getLiveData("state", State.STANDBY)
    val state: LiveData<State> get() = _state

    var isFirstTimeListening: Boolean = true

    var showingHidingOfflineIcon: Boolean = false
    var offlineModeIconVisible: Boolean = false

    private val _currentSentence: MutableLiveData<Sentence> =
        savedStateHandle.getLiveData("sentence")
    val currentSentence: LiveData<Sentence> get() = _currentSentence

    private var currentRecording: Recording?
        get() = savedStateHandle["currentRecording"]
        set(value) {
            savedStateHandle["currentRecording"] = value
        }

    val hasFinishedSentences = sentencesRepository.getLiveSentenceCount().map { it == 0 }

    init {
        mediaRecorderRepository.setupRecorder()
    }

    fun startRecording() {
        if (speakPrefManager.playRecordingSoundIndicator) {
            recordingSoundIndicatorRepository.playStartingSound {
                mediaRecorderRepository.startRecording()
                _state.postValue(State.RECORDING)
            }
        } else {
            mediaRecorderRepository.startRecording()
            _state.postValue(State.RECORDING)
        }
    }

    fun stopRecording() {
        _currentSentence.value?.let { sentence ->
            mediaRecorderRepository.stopRecordingAndReadData(sentence, onError = {

                when (it) {
                    0 -> {
                        // suppress message dialogs
                        _state.postValue(State.SUPPRESSED)
                    }
                    1 -> {
                        _state.postValue(State.RECORDING_ERROR)
                    }
                    2 -> {
                        _state.postValue(State.RECORDING_TOO_LONG)
                    }
                    3 -> {
                        _state.postValue(State.RECORDING_TOO_SHORT)
                    }
                }

                if (speakPrefManager.playRecordingSoundIndicator) {
                    recordingSoundIndicatorRepository.playFinishingSound()
                }

            }, onSuccess = { recording ->

                currentRecording = recording

                if (speakPrefManager.skipRecordingConfirmation) {
                    _state.postValue(State.LISTENED)
                } else {
                    _state.postValue(State.RECORDED)
                }

                if (speakPrefManager.playRecordingSoundIndicator) {
                    recordingSoundIndicatorRepository.playFinishingSound()
                }
            })
        }
    }

    fun skipSentence() = viewModelScope.launch(Dispatchers.IO) {
        when (_state.value) {
            State.RECORDING -> stopRecording()
            State.LISTENING -> stopListening()
        }

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
            mediaPlayerRepository.playRecording(recording, speakPrefManager.audioSpeed)
            _state.postValue(State.LISTENING)
        }
    }

    fun stopListening() {
        mediaPlayerRepository.stopPlaying()
        _state.postValue(State.RECORDED)
    }

    fun redoRecording() {
        this.startRecording()
    }

    fun sendRecording() = viewModelScope.launch(Dispatchers.IO) {
        stopListening()
        currentRecording?.let { recording ->
            var sentence = currentSentence.value
            recordingsRepository.insertRecording(recording)

            currentSentence.value?.let {
                sentence = it
                sentencesRepository.deleteSentence(it)
            }
            currentRecording = null
            withContext(Dispatchers.Main) {
                _state.postValue(State.STANDBY)
                RecordingsUploadWorker.attachToWorkManager(workManager)
                SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
            }

            appActionsRepository.insertAction(
                AppAction.Type.SPEAK_SENT,
                sentenceId = sentence?.sentenceId.toString(),
                clipId = "",
                actionDetails = "sentenceText = ${sentence?.sentenceText}"
            )
        }
    }

    fun loadNewSentence() = viewModelScope.launch(Dispatchers.IO) {
        val sentence = sentencesRepository.getOldestSentence()
        if (sentence != null) {
            _currentSentence.postValue(sentence)
        } else {
            delay(500) //Just to avoid a loop
            _state.postValue(if (speakPrefManager.noMoreSentencesAvailable) State.NO_MORE_SENTENCES else State.STANDBY)
            SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
        }
    }

    fun stop(suppressError: Boolean = false) {
        when (state.value) {
            State.RECORDING -> {
                mediaRecorderRepository.stop(suppressError)
            }
            State.LISTENING -> {
                mediaPlayerRepository.stopPlaying()
            }
        }

        currentRecording = null

        mediaRecorderRepository.clean()
        mediaPlayerRepository.clean()
        recordingSoundIndicatorRepository.clean()

        mediaRecorderRepository.setupRecorder()
    }

    fun reportSentence(reasons: List<String>) = viewModelScope.launch {
        currentSentence.value?.let {
            val sentence = currentSentence.value
            reportsRepository.insertReport(Report(it, reasons))

            appActionsRepository.insertAction(
                AppAction.Type.SPEAK_REPORTED,
                sentenceId = sentence?.sentenceId.toString(),
                clipId = "",
                actionDetails = "sentenceText = ${sentence?.sentenceText}\nreasons = ${reasons.toString()}"
            )
            ReportsUploadWorker.attachToWorkManager(workManager)
            skipSentence()
        }
    }

    suspend fun getSentencesCount() = sentencesRepository.getSentenceCount()

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
            RECORDING_TOO_SHORT,
            RECORDING_TOO_LONG,
            SUPPRESSED,
            NO_MORE_SENTENCES,
        }
    }

}