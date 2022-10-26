package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GenericViewModel: ViewModel() {
    var fromFragment = MutableLiveData<String>()
    fun updateFromFragment(value: String) {
        fromFragment.value = value
    }
}