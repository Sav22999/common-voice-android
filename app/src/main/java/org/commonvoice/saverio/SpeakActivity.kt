package org.commonvoice.saverio

import android.os.Bundle
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SpeakActivity : VariableLanguageActivity(R.layout.activity_test) {

    private val speakViewModel: SpeakViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}