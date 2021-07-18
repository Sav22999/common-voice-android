package org.commonvoice.saverio_lib.background

import androidx.work.*

internal object WorkerUtil {

    val genericConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val wifiOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .build()

    inline fun <reified T : ListenableWorker> genericOneTimeRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<T>()
            .setConstraints(genericConstraint)
            .build()
    }

    inline fun <reified T : ListenableWorker> wifiOnlyOneTimeRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<T>()
            .setConstraints(wifiOnlyConstraint)
            .build()
    }

    inline fun <reified T : ListenableWorker> request(
        wifiOnly: Boolean
    ): OneTimeWorkRequest {
        return if (wifiOnly)
            wifiOnlyOneTimeRequest<T>()
        else
            genericOneTimeRequest<T>()
    }

}