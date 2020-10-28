package org.commonvoice.saverio_lib.api.services

import org.commonvoice.saverio_lib.api.responseBodies.ResponseGithubRelease
import retrofit2.Response
import retrofit2.http.GET

interface GithubService {

    @GET("releases/latest")
    suspend fun getLatestVersion(): Response<ResponseGithubRelease>

}