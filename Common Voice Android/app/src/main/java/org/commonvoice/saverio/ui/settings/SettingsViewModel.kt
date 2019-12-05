package org.commonvoice.saverio.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val selected_language = MutableLiveData<String>().apply {
        value = "it"
    }
    val language: LiveData<String> = selected_language
}