package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.repositories.CVStatsRepository

class LoginViewModel(
    private val database: AppDB,
    private val userRepository: CVStatsRepository,
): ViewModel() {

    fun clearDB() = viewModelScope.launch(Dispatchers.IO) {
        database.clearAllTables()
    }

    fun getUserClient() = liveData {
        emit(userRepository.getUserClient())
    }

}