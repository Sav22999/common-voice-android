package org.commonvoice.saverio_lib.api.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import org.commonvoice.saverio_lib.utils.PrefManager

/**
 * In AuthenticationInterceptor we add the parameters required to authenticate the client to the
 * headers of every request made with Retrofit
 */
class AuthenticationInterceptor(
    private val prefManager: PrefManager
) : Interceptor {

    companion object {
        const val token =
            "Basic OWNlYzFlZTgtZGZmYS00YWU1LWI3M2MtYjg3NjM1OThjYmVjOjAxOTdmODU2NjhlMDQ3NTlhOWUxNzZkM2Q2MDdkOTEzNDE3ZGZkMjA="
    }

    //We recreate the request changing the headers
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().newBuilder().apply {
            if (prefManager.sessIdCookie != null) {
                //If the user is authenticated we use the cookie
                addHeader("Cookie", "connect.sid=${prefManager.sessIdCookie}")
            } else {
                //If the user isn't authenticated we use a generic token
                addHeader("Authorization", token)
            }
        }.build())
    }

}