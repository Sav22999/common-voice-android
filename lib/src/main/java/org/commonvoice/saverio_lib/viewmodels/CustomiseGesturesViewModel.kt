package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CustomiseGesturesViewModel : ViewModel() {
    private val mutableAction = MutableLiveData<String>()

    val action: LiveData<String> get() = mutableAction

    fun changeAction(newAction: String) {
        mutableAction.value = newAction
    }
}
