package org.commonvoice.saverio

import android.app.Application
import android.media.MediaRecorder
import androidx.lifecycle.SavedStateHandle
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.repositories.ClipRepository
import org.commonvoice.saverio_lib.repositories.SentenceRepository
import org.commonvoice.saverio_lib.repositories.SoundListeningRepository
import org.commonvoice.saverio_lib.repositories.SoundRecordingRepository
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

    private val utilsModule = module {
        single { PrefManager(androidContext()) }
    }

    private val apiModules = module {
        single { RetrofitFactory(get()) }
    }

    private val mvvmRepos = module {
        single { ClipRepository(get()) }
        single { SoundRecordingRepository() }
        single { SentenceRepository(get()) }
        single { SoundListeningRepository(androidContext()) }
    }

    private val mvvmViewmodels = module {
        viewModel { (handle: SavedStateHandle) -> SpeakViewModel(handle, get(), get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CommonVoice)
            androidLogger()
            modules(listOf(utilsModule, apiModules, mvvmRepos, mvvmViewmodels))
        }
    }

}