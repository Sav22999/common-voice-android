package org.commonvoice.saverio.util.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import org.commonvoice.saverio.util.api.responses.ClipResponse
import org.commonvoice.saverio.util.repositories.ClipRepository

class ClipViewModel : ViewModel() {

    private val repository = ClipRepository()

    fun sendClip(sentence: String, id: String, auth: String, sound: ByteArray): LiveData<ClipResponse?>
        = liveData(Dispatchers.IO) {
        val response = repository.sendClip(sentence, id, auth, sound)
        emit(response.body())
    }

}