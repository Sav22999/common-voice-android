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
            "Basic MzVmNmFmZTItZjY1OC00YTNhLThhZGMtNzQ0OGM2YTM0MjM3OjNhYzAwMWEyOTQyZTM4YzBiNmQwMWU0M2RjOTk0YjY3NjA0YWRmY2Q="
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