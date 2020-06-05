package org.commonvoice.saverio

import android.os.Bundle
import androidx.lifecycle.Observer
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class ListenActivity : VariableLanguageActivity(R.layout.activity_speak) {

    private val listenViewModel: ListenViewModel by stateViewModel()

    private val connectionManager: ConnectionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialUIState()

        if (true) { //Does ListenActivity need any permission? If not remove this if
            setupUI()
        }
    }

    private fun setupInitialUIState() {

    }

    private fun setupUI() {
        listenViewModel.state.observe(this, Observer { state ->
            when(state) {
                ListenViewModel.Companion.State.STANDBY -> {
                    loadUIStateLoading()

                }
            }
        })
    }

    private fun loadUIStateLoading() {

    }

}