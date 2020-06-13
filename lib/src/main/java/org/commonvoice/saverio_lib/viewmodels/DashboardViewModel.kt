package org.commonvoice.saverio_lib.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import org.commonvoice.saverio_lib.api.responseBodies.ResponseEverStats
import org.commonvoice.saverio_lib.api.responseBodies.ResponseVoicesToday
import org.commonvoice.saverio_lib.models.UserClient
import org.commonvoice.saverio_lib.repositories.CVStatsRepository

class DashboardViewModel(
    private val cvStatsRepository: CVStatsRepository
) : ViewModel() {

    fun getUserClient(): LiveData<UserClient> = liveData {
        cvStatsRepository.getUserClient().body()?.let {
            emit(it)
        }
    }

    fun getDailyClipsCount(): LiveData<Int> = liveData {
        cvStatsRepository.getDailyClipsCount().body()?.let {
            emit(it)
        }
    }

    fun getDailyVotesCount(): LiveData<Int> = liveData {
        cvStatsRepository.getDailyVotesCount().body()?.let {
            emit(it)
        }
    }

    fun getEverCount(): LiveData<ResponseEverStats> = liveData {
        cvStatsRepository.getEverCount().body()?.let {
            it.lastOrNull()?.let {
                emit(it)
            }
        }
    }

    fun getHourlyVoices(): LiveData<List<ResponseVoicesToday>> = liveData {
        cvStatsRepository.getHourlyVoices().body()?.let {
            emit(it.takeLast(2))
        }
    }

}