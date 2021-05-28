package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import androidx.lifecycle.*
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.ReportsUploadWorker
import org.commonvoice.saverio_lib.background.ValidationsUploadWorker
import org.commonvoice.saverio_lib.mediaPlayer.MediaPlayerRepository
import org.commonvoice.saverio_lib.models.AppAction
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.models.Report
import org.commonvoice.saverio_lib.models.Sentence
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.repositories.AppActionsRepository
import org.commonvoice.saverio_lib.repositories.ClipsRepository
import org.commonvoice.saverio_lib.repositories.ReportsRepository
import org.commonvoice.saverio_lib.repositories.ValidationsRepository

class ListenViewModel(
    handle: SavedStateHandle,
    private val clipsRepository: ClipsRepository,
    private val validationsRepository: ValidationsRepository,
    private val mediaPlayerRepository: MediaPlayerRepository,
    private val reportsRepository: ReportsRepository,
    private val workManager: WorkManager,
    private val mainPrefManager: MainPrefManager,
    private val listenPrefManager: ListenPrefManager,
    private val appActionsRepository: AppActionsRepository,
) : ViewModel() {

    private val _state = handle.getLiveData("state", State.STANDBY)
    val state: LiveData<State> get() = _state

    var opened: Boolean = false
    var startedOnce: Boolean = false
    var listenedOnce: Boolean = false
    var stopped: Boolean = false
    var showingHidingOfflineIcon: Boolean = false
    var offlineModeIconVisible: Boolean = false

    private val _currentClip: MutableLiveData<Clip> = handle.getLiveData("currentClip")
    val currentClip: LiveData<Clip> get() = _currentClip

    val hasFinishedClips = clipsRepository.getLiveClipsCount().map { it == 0 }

    fun loadNewClip() = viewModelScope.launch {
        val clip = clipsRepository.getOldestClip(mainPrefManager.language)
        if (clip != null) {
            _currentClip.postValue(clip)
        } else {
            delay(500)
            _state.postValue(if (listenPrefManager.noMoreClipsAvailable) State.NO_MORE_CLIPS else State.STANDBY)
            ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)
        }
    }

    fun skipClip() = viewModelScope.launch {
        stopped = false
        mediaPlayerRepository.stopPlaying()
        currentClip.value?.let {
            clipsRepository.deleteClip(it)
        }
        withContext(Dispatchers.Main) {
            _state.postValue(State.STANDBY)
            ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)
        }
    }

    fun startListening() {
        currentClip.value?.let { clip ->
            mediaPlayerRepository.setup {
                _state.postValue(State.LISTENED)
            }
            if (mediaPlayerRepository.playClip(clip, listenPrefManager.audioSpeed)) {
                _state.postValue(State.LISTENING)
            } else {
                //TODO
                _state.postValue(State.ERROR)
            }
        }
    }

    fun stopListening() {
        mediaPlayerRepository.stopPlaying()
        _state.postValue(State.STANDBY)
        this.stopped = true
    }

    fun validate(result: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        currentClip.value?.let { clip ->
            mediaPlayerRepository.stopPlaying()
            val sentence: Sentence = clip.sentence
            val clipId = clip.glob
            val validation = clip.toValidation(result)

            validationsRepository.insertValidation(validation)
            clipsRepository.deleteClip(clip)

            withContext(Dispatchers.Main) {
                _state.postValue(State.STANDBY)
                ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)
                ValidationsUploadWorker.attachToWorkManager(workManager)
            }

            appActionsRepository.insertAction(
                if (result) AppAction.Type.LISTEN_ACCEPTED else AppAction.Type.LISTEN_REJECTED,
                sentenceId = sentence.sentenceId,
                clipId = clipId,
                actionDetails = "sentenceText = ${sentence.sentenceText}"
            )
        }
        stopped = false
    }

    fun stop() {
        when (state.value) {
            State.LISTENING -> mediaPlayerRepository.stopPlaying()
            else -> {
            }
        }
        mediaPlayerRepository.clean()
    }

    fun reportClip(reasons: List<String>) = viewModelScope.launch {
        currentClip.value?.let {
            val sentence: Sentence = it.sentence
            val clipId = it.glob
            reportsRepository.insertReport(Report(it, reasons))

            appActionsRepository.insertAction(
                AppAction.Type.LISTEN_REPORTED,
                sentenceId = sentence.sentenceId,
                clipId = clipId,
                actionDetails = "sentenceText = ${sentence.sentenceText}\nreasons = ${reasons.toString()}"
            )
            ReportsUploadWorker.attachToWorkManager(workManager)
            skipClip()
        }
    }

    suspend fun getClipsCount() = clipsRepository.getClipsCount()

    fun autoPlay(): Boolean {
        return listenPrefManager.isAutoPlayClipEnabled
    }

    fun showSentencesTextAtTheEnd(): Boolean {
        return listenPrefManager.isShowTheSentenceAtTheEnd
    }

    fun getSentenceText(): String? {
        return currentClip.value?.sentence?.sentenceText
    }

    companion object {
        @Parcelize
        enum class State : Parcelable {
            STANDBY,
            LISTENING,
            LISTENED,
            ERROR,
            NO_MORE_CLIPS,
        }
    }

}