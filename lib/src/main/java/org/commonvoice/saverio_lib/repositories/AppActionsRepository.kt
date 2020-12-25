package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.AppAction
import org.commonvoice.saverio_lib.preferences.MainPrefManager

class AppActionsRepository(
    database: AppDB,
    private val prefManager: MainPrefManager,
    private val connectionManager: ConnectionManager?,
) {

    private val actionsDao = database.appActions()

    suspend fun insertAction(actionType: AppAction.Type) {
        if (prefManager.areAppUsageStatsEnabled && connectionManager != null) {
            insertAction(
                AppAction(
                    0,
                    prefManager.language,
                    actionType,
                    !connectionManager.isInternetAvailable
                )
            )
        }
    }

    @WorkerThread
    private suspend fun insertAction(action: AppAction) = actionsDao.insertAction(action)

    @WorkerThread
    suspend fun deleteAction(action: AppAction) = actionsDao.deleteAction(action)

    @WorkerThread
    suspend fun getAllActions() = actionsDao.getAll()

}