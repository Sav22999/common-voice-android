package org.commonvoice.saverio.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Here show the Dashboard of Common Voice web site"
    }
    val text: LiveData<String> = _text
}