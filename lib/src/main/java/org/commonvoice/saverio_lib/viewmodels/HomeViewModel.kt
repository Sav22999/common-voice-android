package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.preferences.LogPrefManager
import org.commonvoice.saverio_lib.repositories.FileLogsRepository
import org.commonvoice.saverio_lib.repositories.StatsRepository
import timber.log.Timber

class HomeViewModel(
    private val statsRepository: StatsRepository,
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

    fun postFileLog(
        appVersion: String,
        versionCode: Int,
        appSource: String
    ) {
        viewModelScope.launch {
            if (!logPrefManager.isLogFileSent) {
                fileLogsRepository.postFileLog(
                    appVersion,
                    versionCode.toString(),
                    appSource,
                    logPrefManager.stackTrace
                )
            }
        }
    }
}