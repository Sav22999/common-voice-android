package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.repositories.StatsRepository

class MainActivityViewModel(
    private val statsRepository: StatsRepository,
    private val database: AppDB
) : ViewModel() {

    fun postStats(appVersion: String, appSource: String) = viewModelScope.launch {
        statsRepository.postStatsUpdate(appVersion, appSource)
    }

    fun clearDB() = viewModelScope.launch(Dispatchers.IO) {
        database.clearAllTables()
    }
}