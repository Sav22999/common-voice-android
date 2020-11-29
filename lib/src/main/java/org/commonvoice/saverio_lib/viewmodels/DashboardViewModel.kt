package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.api.responseBodies.ResponseLeaderboardPosition
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.repositories.CVStatsRepository
import org.commonvoice.saverio_lib.repositories.StatsRepository
import org.commonvoice.saverio_lib.utils.combineLiveData

class DashboardViewModel(
    private val cvStatsRepository: CVStatsRepository,
    private val statsRepository: StatsRepository,
    private val connectionManager: ConnectionManager,
    private val mainPrefManager: MainPrefManager,
    private val statsPrefManager: StatsPrefManager
) : ViewModel() {

    private val _stats = MutableLiveData<Stats>()
    val stats: LiveData<Stats> get() = _stats

    private val _onlineVoices = MutableLiveData<OnlineVoices>()
    val onlineVoices: LiveData<OnlineVoices> get() = _onlineVoices

    private val _contributors = MutableLiveData<Contributors>()
    val contributorsIsInSpeak = MutableLiveData(true)
    val contributors = combineLiveData(
        _contributors,
        contributorsIsInSpeak,
        Contributors(listOf(), listOf()),
        true
    )

    private val _usage = MutableLiveData<AppUsage>()
    val usage: LiveData<AppUsage> get() = _usage


    var lastStatsUpdate: Long = 0

    fun updateStats(force: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if ((System.currentTimeMillis() - lastStatsUpdate >= 30000 && connectionManager.isInternetAvailable) || force){
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

            val topContributorsSpeak = async {
                cvStatsRepository.getRecordingsLeaderboard()
            }
            val topContributorsListen = async {
                cvStatsRepository.getClipsLeaderboard()
            }

            val appUsage = async {
                statsRepository.getStats()
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

            _contributors.postValue(Contributors(
                topContributorsSpeak.await(),
                topContributorsListen.await()
            ))

            _usage.postValue(appUsage.await().let { usage ->
                AppUsage(
                    usage[mainPrefManager.language]?.users ?: -1,
                    usage.values.sumBy { it.users }
                )
            })

        } else {
            _stats.value?.let {
                _stats.postValue(it)
            }
            _contributors.value?.let {
                _contributors.postValue(it)
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

    data class Contributors(
        val topContributorsSpeak: List<ResponseLeaderboardPosition>,
        val topContributorsListen: List<ResponseLeaderboardPosition>
    )

    data class AppUsage(
        val languageUsage: Int,
        val totalUsage: Int
    )

}