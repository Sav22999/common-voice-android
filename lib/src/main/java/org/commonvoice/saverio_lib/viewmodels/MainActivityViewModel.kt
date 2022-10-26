package org.commonvoice.saverio_lib.viewmodels

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.UserClient
import org.commonvoice.saverio_lib.repositories.CVStatsRepository
import org.commonvoice.saverio_lib.repositories.StatsRepository

class MainActivityViewModel(
    private val statsRepository: StatsRepository,
    private val userRepository: CVStatsRepository,
    private val database: AppDB,
) : ViewModel() {
    fun postStats(
        appVersion: String,
        versionCode: Int,
        appSource: String
    ) = viewModelScope.launch {
        statsRepository.postStatsUpdate(appVersion, versionCode.toString(), appSource)
    }

    fun clearDB() = viewModelScope.launch(Dispatchers.IO) {
        database.clearAllTables()
    }

    fun getUserClient(): LiveData<UserClient?> = liveData {
        emit(
            try {
                userRepository.getUserClient()
            } catch (e: Exception) {
                Log.e(
                    "MainActivityViewModel",
                    "Exception ${e.stackTrace}. Probably Internet is not available."
                )
                null
            }
        )
    }

}