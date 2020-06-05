package org.commonvoice.saverio_lib.viewmodels

import android.os.Parcelable
import androidx.lifecycle.*
import androidx.work.WorkManager
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.mediaPlayer.MediaPlayerRepository
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.repositories.ClipsRepository
import org.commonvoice.saverio_lib.repositories.ValidationsRepository

class ListenViewModel(
    handle: SavedStateHandle,
    private val clipsRepository: ClipsRepository,
    private val validationsRepository: ValidationsRepository,
    private val mediaPlayerRepository: MediaPlayerRepository,
    private val workManager: WorkManager,
    private val mainPrefManager: MainPrefManager,
    private val listenPrefManager: ListenPrefManager,
    private val statsPrefManager: StatsPrefManager
): ViewModel() {

    private val _state = handle.getLiveData("state", State.STANDBY)
    val state: LiveData<State> get() = _state

    private val _currentClip: MutableLiveData<Clip> = handle.getLiveData("currentClip")
    val currentClip: LiveData<Clip> get() = _currentClip

    fun loadNewClip() = viewModelScope.launch {

    }

    companion object {
        @Parcelize
        enum class State: Parcelable {
            STANDBY
        }
    }

}