package org.commonvoice.saverio.utils

import org.commonvoice.saverio_lib.repositories.ClipsRepository
import org.commonvoice.saverio_lib.repositories.RecordingsRepository
import org.commonvoice.saverio_lib.repositories.SentenceRepository
import org.commonvoice.saverio_lib.repositories.ValidationsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class DatabaseWorker: KoinComponent {

    private var isClipsDoingWork = false
    private val clipsRepository: ClipsRepository by inject()

    private var isRecordingsDoingWork = false
    private val recordingsRepository: RecordingsRepository by inject()

    private var isSentencesDoingWork = false
    private val sentenceRepository: SentenceRepository by inject()

    private var isValidationsDoingWork = false
    private val validationsRepository: ValidationsRepository by inject()

    fun start() {
        setupClipsWorker()
        setupRecordingsWorker()
        setupSentencesWorker()
        setupValidationsWorker()
    }

    private fun setupClipsWorker() {
        clipsRepository.getLiveClipsCount().observeForever {

        }
    }

    private fun setupRecordingsWorker() {

    }

    private fun setupSentencesWorker() {

    }

    private fun setupValidationsWorker() {

    }

    companion object {

        const val dbWantedSize = 20

    }

}