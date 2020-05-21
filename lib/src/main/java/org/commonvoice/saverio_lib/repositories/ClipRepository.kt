package org.commonvoice.saverio_lib.repositories

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.responses.RecordingSent
import retrofit2.Response
import java.io.File
import java.net.URLEncoder

class ClipRepository(factory: RetrofitFactory) {

    private val clipService = factory.makeClipService()

    suspend fun sendRecording(sentence: String, id: String, file: File): Response<RecordingSent> {
        return try {
            val encodedSentence = URLEncoder.encode(sentence, "UTF-8").replace("+", "%20")
            clipService.sendRecording(
                encodedSentence,
                id,
                file.asRequestBody("audio/mpeg".toMediaType())
            )
        } catch (e: Exception) {
            Response.error(500, "".toResponseBody())
        }
    }

}