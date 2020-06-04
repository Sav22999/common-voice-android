package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio_lib.db.AppDB

class LoginViewModel(
    private val database: AppDB
): ViewModel() {

    fun clearDB() = viewModelScope.launch(Dispatchers.IO) {
        database.clearAllTables()
    }

}