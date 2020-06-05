package org.commonvoice.saverio

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_listen.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.models.Clip
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class ListenActivity : VariableLanguageActivity(R.layout.activity_listen) {

    private val listenViewModel: ListenViewModel by stateViewModel()

    private val connectionManager: ConnectionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        setupUI()

        connectionManager.liveInternetAvailability.observe(this, Observer {

        })
    }

    private fun setupInitialUIState() {
        btn_skip_listen.onClick {
            listenViewModel.skipClip()
        }

        btn_yes_thumb.isVisible = false
        btn_no_thumb.isVisible = false
    }

    private fun setupUI() {
        listenViewModel.currentClip.observe(this, Observer { clip ->
            loadUIStateStandby(clip)
        })

        listenViewModel.state.observe(this, Observer { state ->
            when(state) {
                ListenViewModel.Companion.State.STANDBY -> {
                    loadUIStateLoading()
                    listenViewModel.loadNewClip()
                }
                ListenViewModel.Companion.State.LISTENING -> {
                    loadUIStateListening()
                }
                ListenViewModel.Companion.State.LISTENED -> {
                    loadUIStateListened()
                }
            }
        })
    }

    private fun loadUIStateLoading() {
        textSentenceListen.setText(R.string.txt_loading_sentence)
    }

    private fun loadUIStateStandby(clip: Clip) {
        textSentenceListen.text = clip.sentence.sentenceText

        btn_start_listen.onClick {
            listenViewModel.startListening()
        }

        btn_yes_thumb.isVisible = false
        btn_no_thumb.isVisible = false
    }

    private fun loadUIStateListening() {
        btn_no_thumb.isVisible = true
        btn_yes_thumb.isVisible = false

        btn_no_thumb.onClick {
            listenViewModel.validate(result = false)
        }
    }

    private fun loadUIStateListened() {
        btn_yes_thumb.isVisible = true
        btn_no_thumb.isVisible = true

        btn_yes_thumb.onClick {
            listenViewModel.validate(result = true)
        }
    }

}