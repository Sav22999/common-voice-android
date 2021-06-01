package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.models.Message
import org.commonvoice.saverio_lib.preferences.LogPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.repositories.FileLogsRepository
import org.commonvoice.saverio_lib.repositories.GithubRepository
import org.commonvoice.saverio_lib.repositories.StatsRepository

class HomeViewModel(
    private val prefManager: MainPrefManager,
    private val statsRepository: StatsRepository,
    private val githubRepository: GithubRepository,
    private val settingsPrefManager: SettingsPrefManager,
    private val fileLogsRepository: FileLogsRepository,
    private val logPrefManager: LogPrefManager
) : ViewModel() {

    fun postStats(
        appVersion: String,
        versionCode: Int,
        appSource: String
    ) = viewModelScope.launch {
        statsRepository.postStatsUpdate(appVersion, versionCode.toString(), appSource)
    }

    fun checkForNewVersion(localVersion: String): LiveData<String> = liveData {
        val serverVersion = try {
            githubRepository.getLatestVersion().body()?.latestVersion
        } catch (e: Exception) {
            null
        } ?: localVersion

        if (serverVersion != settingsPrefManager.latestVersion && serverVersion != localVersion) {
            settingsPrefManager.latestVersion = serverVersion

            emit(serverVersion)
        }
    }

    fun postFileLog(
        versionCode: Int,
        appSource: String
    ) = viewModelScope.launch {
        if (!logPrefManager.isLogFileSent) {
            fileLogsRepository.postFileLog(
                versionCode.toString(),
                appSource,
                logPrefManager.stackTrace
            )
        }
    }

    /*
    type=null | Banner
    type=0 | Standard
    type=1 | Info
    type=2 | Warning
    type=3 | News/Changelog
    type=4 | Tip
     */

    fun getLastBannerMessage(): LiveData<Message> = liveData {
        statsRepository.getNewMessages()
            .filter { it.type == null }
            .filterNot { it.id in prefManager.shownMessagesId }
            .firstOrNull()
            ?.let {
                emit(it)
            }
    }

    fun getStandardMessages(): LiveData<List<Message>> = liveData {
        statsRepository.getNewMessages()
            .filter { it.type == 0 }
            .filterNot { it.id in prefManager.shownMessagesId }
            .let {
                emit(it)
            }
    }

    fun getInfoMessages(): LiveData<List<Message>> = liveData {
        statsRepository.getNewMessages()
            .filter { it.type == 1 }
            .filterNot { it.id in prefManager.shownMessagesId }
            .let {
                emit(it)
            }
    }

    fun getWarningMessages(): LiveData<List<Message>> = liveData {
        statsRepository.getNewMessages()
            .filter { it.type == 2 }
            .filterNot { it.id in prefManager.shownMessagesId }
            .let {
                emit(it)
            }
    }

    fun getOtherMessages(): LiveData<List<Message>> = liveData {
        statsRepository.getNewMessages()
            .filter { it.type != null && it.type != 1 && it.type != 2 }
            .filterNot { it.id in prefManager.shownMessagesId }
            .let {
                emit(it)
            }
    }

    fun markMessageAsSeen(message: Message) {
        prefManager.shownMessagesId = prefManager.shownMessagesId.toMutableList()
            .also { it.add(message.id) }
    }

}