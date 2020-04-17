package org.commonvoice.saverio.util.repositories

import android.util.Log
import androidx.annotation.WorkerThread
import okhttp3.MediaType
import okhttp3.RequestBody
import org.commonvoice.saverio.util.api.RetrofitFactory
import org.commonvoice.saverio.util.api.responses.ClipResponse
import org.commonvoice.saverio.util.utils.getFakeResponse
import retrofit2.Response
import java.net.URLEncoder
import java.nio.charset.Charset

class ClipRepository {

    private val service = RetrofitFactory.makeClipService()

    @WorkerThread
    suspend fun sendClip(sentence: String, id: String, auth: String, sound: ByteArray): Response<ClipResponse> {
        val body = RequestBody.create(MediaType.get("audio/ogg; codecs=opus4"), sound)
        val encodedSentence = URLEncoder.encode(sentence, "UTF-8").replace("+", "%20")
        return try {
            service.sendClip(encodedSentence, id, auth, null, sound.size, body)
        } catch (e: Exception) {
            Log.wtf("Exception", e.toString())
            getFakeResponse()
        }
    }

}