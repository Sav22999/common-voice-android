package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentSpeakSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class SpeakSettingsFragment : ViewBoundFragment<FragmentSpeakSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSpeakSettingsBinding {
        return FragmentSpeakSettingsBinding.inflate(layoutInflater, container, false)
    }

}