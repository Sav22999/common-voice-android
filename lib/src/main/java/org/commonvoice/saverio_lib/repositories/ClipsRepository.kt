package org.commonvoice.saverio_lib.repositories

import androidx.annotation.WorkerThread
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.db.AppDB
import org.commonvoice.saverio_lib.models.Clip
import java.sql.Timestamp

class ClipsRepository(database: AppDB, retrofitFactory: RetrofitFactory) {

    private val clipsDao = database.clips()

    private val clipsClient = retrofitFactory.makeClipsService()
    private val clipsDownloadClient = retrofitFactory.makeClipsDownloadService()

    suspend fun getNewClips(count: Int): List<Clip>? {
        val retrofitClips = clipsClient.getClips(count)

        return retrofitClips.body()?.map {
            val audioByteArray = downloadAudioClip(it.audioSrc)
            if (audioByteArray != null) {
                it.toClip(audioByteArray)
            } else {
                return null
            }
        }
    }

    private suspend fun downloadAudioClip(url: String): ByteArray? {
        val response = clipsDownloadClient.downloadAudioFile(url)

        return response.body()?.bytes()
    }

    @WorkerThread
    suspend fun insertClip(clip: Clip) = clipsDao.insertClip(clip)

    @WorkerThread
    suspend fun insertClips(clips: List<Clip>) = clipsDao.insertClips(clips)

    @WorkerThread
    suspend fun deleteClip(clip: Clip) = clipsDao.deleteClip(clip)

    @WorkerThread
    suspend fun getClipsCount() = clipsDao.getCount()

    fun getLiveClipsCount() = clipsDao.getLiveCount()

    @WorkerThread
    suspend fun getOldestClip() = clipsDao.getOldestClip()

    @WorkerThread
    suspend fun getOldClips(dateOfToday: Timestamp) = clipsDao.getOldClips(dateOfToday.time)

    @WorkerThread
    suspend fun deleteOldClips(dateOfToday: Timestamp) = clipsDao.deleteOldClips(dateOfToday.time)

}