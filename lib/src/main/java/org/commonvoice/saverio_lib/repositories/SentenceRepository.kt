package org.commonvoice.saverio_lib.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.responses.RecordingSent
import retrofit2.Response
import java.io.File
import java.net.URLEncoder

class SentenceRepository(factory: RetrofitFactory) {

    private val sentenceClient = factory.makeSentenceService()

    suspend fun sendRecording(sentence: String, id: String, file: File): Response<RecordingSent> {
        return try {
            Log.wtf("SpeakViewModel", "Sent recording 2")
            val encodedSentence = URLEncoder.encode(sentence, "UTF-8").replace("+", "%20")
            Log.wtf("SpeakViewModel", "Sent recording 3")
            sentenceClient.sendRecording(
                encodedSentence,
                id,
                file.asRequestBody("audio/mpeg".toMediaType())
            )
        } catch (e: Exception) {
            Response.error(500, "".toResponseBody())
        }
    }

}