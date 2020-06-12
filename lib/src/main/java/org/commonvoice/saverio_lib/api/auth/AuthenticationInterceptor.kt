package org.commonvoice.saverio_lib.api.auth

import okhttp3.Interceptor
import okhttp3.Response
import org.commonvoice.saverio_lib.preferences.MainPrefManager

/**
 * In AuthenticationInterceptor we add the parameters required to authenticate the client to the
 * headers of every request made with Retrofit
 */
class AuthenticationInterceptor(
    private val mainPrefManager: MainPrefManager
) : Interceptor {

    private val token: String

    init {
        if (mainPrefManager.tokenUserId == "" || mainPrefManager.tokenAuth == "") {
            val pair = AuthPairManager.generateAuthPair()
            val (userId, authToken) = pair
            mainPrefManager.tokenUserId = userId
            mainPrefManager.tokenAuth = authToken
            token = "Basic ${AuthPairManager.encodeAuthPair(pair)}"
        } else {
            val pair = Pair(mainPrefManager.tokenUserId, mainPrefManager.tokenAuth)
            token = "Basic ${AuthPairManager.encodeAuthPair(pair)}"
        }
    }

    //We recreate the request changing the headers
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().newBuilder().apply {
            if (mainPrefManager.sessIdCookie != null) {
                //If the user is authenticated we use the cookie
                addHeader("Cookie", "connect.sid=${mainPrefManager.sessIdCookie}")
            } else {
                //If the user isn't authenticated we use a generic token
                addHeader("Authorization",
                    token
                )
            }
        }.build())
    }

}