package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.api.responseBodies.ResponseEverStats
import org.commonvoice.saverio_lib.api.responseBodies.ResponseVoicesToday
import org.commonvoice.saverio_lib.models.UserClient
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.repositories.CVStatsRepository

class DashboardViewModel(
    private val cvStatsRepository: CVStatsRepository,
    private val connectionManager: ConnectionManager,
    private val statsPrefManager: StatsPrefManager
) : ViewModel() {

    private val _stats = MutableLiveData<Stats>()
    val stats: LiveData<Stats> get() = _stats

    private val _onlineVoices = MutableLiveData<OnlineVoices>()
    val onlineVoices: LiveData<OnlineVoices> get() = _onlineVoices

    var lastStatsUpdate: Long = 0

    fun updateStats() = viewModelScope.launch(Dispatchers.IO) {
        if (System.currentTimeMillis() - lastStatsUpdate >= 30000 && connectionManager.isInternetAvailable) {
            lastStatsUpdate = System.currentTimeMillis()

            val dailyVotes = async {
                cvStatsRepository.getDailyVotesCount()
            }
            val dailyClips = async {
                cvStatsRepository.getDailyClipsCount()
            }
            val everCount = async {
                cvStatsRepository.getEverCount()
            }
            val userClient = async {
                cvStatsRepository.getUserClient()
            }

            _stats.postValue(Stats(
                dailyVotes.await(),
                dailyClips.await(),
                everCount.await().total,
                everCount.await().valid,
                statsPrefManager.todayValidated,
                statsPrefManager.todayRecorded,
                userClient.await()?.votes_count,
                userClient.await()?.clips_count
            ))

            cvStatsRepository.getHourlyVoices().body()?.let {
                it.takeLast(2).let {
                    _onlineVoices.postValue(
                        OnlineVoices(
                            it.lastOrNull()?.count ?: 0, it.firstOrNull()?.count ?: 0
                        )
                    )
                }
            }
        } else {
            _stats.value?.let {
                _stats.postValue(it)
            }
        }
    }

    data class Stats(
        val everyoneTodayListen: Int,
        val everyoneTodaySpeak: Int,
        val everyoneEverSpeak: Int,
        val everyoneEverListen: Int,

        val userTodayListen: Int,
        val userTodaySpeak: Int,
        val userEverListen: Int?,
        val userEverSpeak: Int?
    )

    data class OnlineVoices(
        val now: Int,
        val before: Int
    )

}