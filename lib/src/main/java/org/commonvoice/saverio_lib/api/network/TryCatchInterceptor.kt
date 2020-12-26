package org.commonvoice.saverio_lib.api.network

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class TryCatchInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: Exception) {
            Response.Builder()
                .code(500)
                .body("".toResponseBody(null))
                .protocol(Protocol.HTTP_2)
                .message("Error 500 - ${e.message}")
                .request(chain.request())
                .build()
        }
    }

}