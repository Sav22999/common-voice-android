package org.commonvoice.saverio_lib.repositories

import okhttp3.ResponseBody.Companion.toResponseBody
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.models.RecordableSentence
import retrofit2.Response

class SentenceRepository(retrofitFactory: RetrofitFactory) {

    private val sentenceClient = retrofitFactory.makeSentenceService()

    suspend fun getSentences(): Response<List<RecordableSentence>> = try {
        sentenceClient.getSentence()
    } catch (e: Exception) {
        Response.error(500, "".toResponseBody())
    }

}