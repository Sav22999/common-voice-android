package org.commonvoice.saverio_lib.repositories

import org.commonvoice.saverio_lib.api.RetrofitFactory

class GithubRepository(
    retrofitFactory: RetrofitFactory
) {

    private val client = retrofitFactory.makeGithubService()

    suspend fun getLatestVersion() = client.getLatestVersion()

}