package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import androidx.lifecycle.*
import androidx.work.WorkManager
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.ReportsUploadWorker
import org.commonvoice.saverio_lib.background.ValidationsUploadWorker
import org.commonvoice.saverio_lib.mediaPlayer.MediaPlayerRepository
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.models.Report
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
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
    private val listenPrefManager: ListenPrefManager
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

    fun loadNewClip() = viewModelScope.launch {
        val clip = clipsRepository.getOldestClip()
        if (clip != null) {
            _currentClip.postValue(clip)
        } else {
            delay(500)
            _state.postValue(State.STANDBY)
        }
    }

    fun skipClip() = viewModelScope.launch {
        stopped = false
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
            mediaPlayerRepository.playClip(clip)
            _state.postValue(State.LISTENING)
        }
    }

    fun stopListening() {
        mediaPlayerRepository.stopPlaying()
        _state.postValue(State.STANDBY)
        this.stopped = true
    }

    fun validate(result: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        currentClip.value?.let { clip ->
            val validation = clip.toValidation(result)

            validationsRepository.insertValidation(validation)
            clipsRepository.deleteClip(clip)

            withContext(Dispatchers.Main) {
                _state.postValue(State.STANDBY)
                ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)
                ValidationsUploadWorker.attachToWorkManager(workManager)
            }
        }
        stopped = false
    }

    fun stop() {
        when (state.value) {
            ListenViewModel.Companion.State.LISTENING -> mediaPlayerRepository.stopPlaying()
        }
        mediaPlayerRepository.clean()
    }

    fun reportClip(reasons: List<String>) = viewModelScope.launch {
        currentClip.value?.let {
            reportsRepository.insertReport(Report(it, reasons))
            ReportsUploadWorker.attachToWorkManager(workManager)
            skipClip()
        }
    }

    fun autoPlay(): Boolean {
        return listenPrefManager.isAutoPlayClipEnabled
    }

    companion object {
        @Parcelize
        enum class State : Parcelable {
            STANDBY,
            LISTENING,
            LISTENED
        }
    }

}