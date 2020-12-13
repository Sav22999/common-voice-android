package org.commonvoice.saverio_lib.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.requestBodies.RetrofitFileLogUpdate
import org.commonvoice.saverio_lib.preferences.LogPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.utils.getTimestampOfNowPlus

class FileLogsRepository(
    private val mainPrefManager: MainPrefManager,
    retrofitFactory: RetrofitFactory,
    private val logPrefManager: LogPrefManager
) {

    private val fileLogClient = retrofitFactory.makeFileLogService()

    suspend fun postFileLog(
        versionCode: String,
        appSource: String,
        stackTrace: String,
    ) = withContext(Dispatchers.IO) {

        val bodyInfo = RetrofitFileLogUpdate(
            isLogged(),
            mainPrefManager.language,
            versionCode,
            appSource,
            stackTrace,
            getTimestampOfNowPlus().toString()
        )
        try {
            val response = fileLogClient.postFileLog(bodyInfo)
            if (response.isSuccessful && response.body()?.resultCode == 200) {
                logPrefManager.isLogFileSent = true
            }
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