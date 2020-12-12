package org.commonvoice.saverio_lib.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitFileLogUpdate
import org.commonvoice.saverio_lib.preferences.LogPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager

class FileLogsRepository(
    private val mainPrefManager: MainPrefManager,
    retrofitFactory: RetrofitFactory,
    private val logPrefManager: LogPrefManager
) {

    private val fileLogClient = retrofitFactory.makeFileLogService()

    suspend fun postFileLog(
        appVersion: String,
        versionCode: String,
        appSource: String,
        stackTrace: String,
    ) = withContext(Dispatchers.IO) {
        if (appVersion.contains('a') || appVersion.contains('b')) return@withContext

        val bodyInfo = RetrofitFileLogUpdate(
            isLogged(),
            mainPrefManager.language,
            versionCode,
            appSource,
            stackTrace
        )
        try {
            logPrefManager.isLogFileSent = true
            fileLogClient.postFileLog(bodyInfo)
        } catch (e: Exception) {

        }
    }

    private fun isLogged(): Int {
        return if (mainPrefManager.sessIdCookie == null) {
            0
        } else {
            1
        }
    }
}