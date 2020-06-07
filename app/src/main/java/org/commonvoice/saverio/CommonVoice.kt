package org.commonvoice.saverio

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.mediaPlayer.MediaPlayerRepository
import org.commonvoice.saverio_lib.mediaPlayer.RecordingSoundIndicatorRepository
import org.commonvoice.saverio_lib.mediaRecorder.FileHolder
import org.commonvoice.saverio_lib.mediaRecorder.MediaRecorderRepository
import org.commonvoice.saverio_lib.preferences.*
import org.commonvoice.saverio_lib.repositories.*
import org.commonvoice.saverio_lib.viewmodels.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Inside the Application we start Koin, so we can do Dependency Injection
 */
@Suppress("RemoveExplicitTypeArguments")
class CommonVoice : Application() {

    private val dbModule = module {
        single { AppDB.build(androidContext()) }
    }

    private val utilsModule = module {
        factory { WorkManager.getInstance(androidContext()) }
        single { FileHolder(androidContext()) }
        single(createdAtStart = true) { ConnectionManager(androidContext()) }
    }

    private val prefsModule = module {
        single {
            MainPrefManager(
                androidContext()
            )
        }
        single {
            FirstRunPrefManager(
                androidContext()
            )
        }
        single {
            SpeakPrefManager(
                androidContext()
            )
        }
        single {
            ListenPrefManager(
                androidContext()
            )
        }
        single {
            StatsPrefManager(
                androidContext()
            )
        }
    }

    private val apiModules = module {
        single { RetrofitFactory(get()) }
    }

    private val mvvmRepos = module {
        single { MediaRecorderRepository(get()) }
        factory { MediaPlayerRepository() }
        single { ClipsRepository(get(), get()) }
        single { RecordingsRepository(get(), get()) }
        single { SentencesRepository(get(), get()) }
        single { ValidationsRepository(get(), get()) }
        single { ReportsRepository(get(), get()) }
        single { StatsRepository(get(), get()) }
        single { RecordingSoundIndicatorRepository(get()) }
    }

    private val mvvmViewmodels = module {
        viewModel { (handle: SavedStateHandle) ->
            SpeakViewModel(
                handle,
                get<SentencesRepository>(),
                get<RecordingsRepository>(),
                get<MediaRecorderRepository>(),
                get<MediaPlayerRepository>(),
                get<RecordingSoundIndicatorRepository>(),
                get<ReportsRepository>(),
                get<WorkManager>(),
                get<SpeakPrefManager>()
            )
        }
        viewModel { (handle: SavedStateHandle) ->
            ListenViewModel(
                handle,
                get<ClipsRepository>(),
                get<ValidationsRepository>(),
                get<MediaPlayerRepository>(),
                get<ReportsRepository>(),
                get<WorkManager>(),
                get<ListenPrefManager>()
            )
        }
        viewModel { LoginViewModel(get()) }
        viewModel { MainActivityViewModel(get(), get()) }
        viewModel { HomeViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CommonVoice)
            androidLogger()
            modules(
                listOf(
                    prefsModule,
                    dbModule,
                    utilsModule,
                    apiModules,
                    mvvmRepos,
                    mvvmViewmodels
                )
            )
        }

    }

}