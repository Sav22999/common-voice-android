package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.api.responseBodies.ResponseLanguage
import retrofit2.Response
import retrofit2.http.GET

interface LanguagesService {

    @GET("v2/languages")
    suspend fun getAvailableLanguages(): Response<ResponseLanguage>

}