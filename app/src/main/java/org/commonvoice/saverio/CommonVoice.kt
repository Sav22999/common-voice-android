package org.commonvoice.saverio

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.mediaPlayer.MediaPlayerRepository
import org.commonvoice.saverio_lib.mediaRecorder.FileHolder
import org.commonvoice.saverio_lib.mediaRecorder.MediaRecorderRepository
import org.commonvoice.saverio_lib.repositories.*
import org.commonvoice.saverio_lib.utils.PrefManager
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Inside the Application we start Koin, so we can do Dependency Injection
 */
class CommonVoice : Application() {

    private val dbModule = module {
        single { AppDB.build(androidContext()) }
    }

    private val utilsModule = module {
        factory { WorkManager.getInstance(androidContext()) }
        single { PrefManager(androidContext()) }
        single { FileHolder(androidContext()) }
    }

    private val apiModules = module {
        single { RetrofitFactory(get()) }
    }

    private val mvvmRepos = module {
        single { MediaRecorderRepository(get()) }
        single { MediaPlayerRepository(androidContext()) }
        single { ClipsRepository(get(), get()) }
        single { RecordingsRepository(get(), get()) }
        single { SentencesRepository(get(), get()) }
        single { ValidationsRepository(get()) }
        single { ReportsRepository(get(), get()) }
    }

    private val mvvmViewmodels = module {
        viewModel { (handle: SavedStateHandle) -> SpeakViewModel(handle, get(), get(), get(), get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CommonVoice)
            androidLogger()
            modules(listOf(dbModule, utilsModule, apiModules, mvvmRepos, mvvmViewmodels))
        }

    }

}