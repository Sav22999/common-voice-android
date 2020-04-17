package org.commonvoice.saverio.util.utils

import okhttp3.ResponseBody
import retrofit2.Response

fun <T> getFakeResponse() = Response.error<T>(500, ResponseBody.create(null, ""))