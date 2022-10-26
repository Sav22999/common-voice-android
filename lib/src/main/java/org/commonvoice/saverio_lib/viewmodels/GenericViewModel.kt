package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GenericViewModel : ViewModel() {
    var fromFragment = MutableLiveData<String>()
    var nestedFragment = MutableLiveData<String>()

    fun setFromFragment(value: String) {
        fromFragment.value = value
        nestedFragment.value = ""
    }

    fun setNestedFragment(value: String) {
        nestedFragment.value = value
    }
}