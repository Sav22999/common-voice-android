package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.repositories.StatsRepository

class HomeViewModel(
    private val statsRepository: StatsRepository
): ViewModel() {

    fun postStats(
        appVersion: String,
        versionCode: Int,
        appSource: String
    ) = viewModelScope.launch {
        statsRepository.postStatsUpdate(appVersion, versionCode.toString(), appSource)
    }

}