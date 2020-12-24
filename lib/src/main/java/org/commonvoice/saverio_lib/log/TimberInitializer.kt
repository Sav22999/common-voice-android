package org.commonvoice.saverio_lib.log

import android.content.Context
import androidx.startup.Initializer
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Timber.plant(FileLogTree(context))
        Timber.plant(Timber.DebugTree())
        Timber.d("TimberInitializer is initialized.")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        emptyList()
}
