package org.commonvoice.saverio_lib.api.auth

import okhttp3.Interceptor
import okhttp3.Response
import org.commonvoice.saverio_lib.preferences.PrefManager

/**
 * In AuthenticationInterceptor we add the parameters required to authenticate the client to the
 * headers of every request made with Retrofit
 */
class AuthenticationInterceptor(
    private val prefManager: PrefManager
) : Interceptor {

    private val token: String

    init {
        if (prefManager.tokenUserId == "" || prefManager.tokenAuth == "") {
            val pair = AuthPairManager.generateAuthPair()
            val (userId, authToken) = pair
            prefManager.tokenUserId = userId
            prefManager.tokenAuth = authToken
            token = "Basic ${AuthPairManager.encodeAuthPair(pair)}"
        } else {
            val pair = Pair(prefManager.tokenUserId, prefManager.tokenAuth)
            token = "Basic ${AuthPairManager.encodeAuthPair(pair)}"
        }
    }

    //We recreate the request changing the headers
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().newBuilder().apply {
            if (prefManager.sessIdCookie != null) {
                //If the user is authenticated we use the cookie
                addHeader("Cookie", "connect.sid=${prefManager.sessIdCookie}")
            } else {
                //If the user isn't authenticated we use a generic token
                addHeader("Authorization",
                    token
                )
            }
        }.build())
    }

}