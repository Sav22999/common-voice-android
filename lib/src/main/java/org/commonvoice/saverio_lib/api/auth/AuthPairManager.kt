package org.commonvoice.saverio_lib.api.auth

import okio.ByteString.Companion.encode
import java.util.*


object AuthPairManager {

    private fun generateUserId(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateAuthToken(): String {
        val charPool = ('a' .. 'z') + ('0' .. '9')
        return List(40) { charPool.random() }.joinToString("")
    }

    fun generateAuthPair(): Pair<String, String> {
        return Pair(generateUserId(), generateAuthToken())
    }

    fun encodeAuthPair(pair: Pair<String, String>): String {
        val plainToken = "${pair.first}:${pair.second}"
        return plainToken.encode().base64()
    }

}