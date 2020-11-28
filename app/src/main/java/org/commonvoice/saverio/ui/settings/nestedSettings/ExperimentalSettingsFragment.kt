package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentExperimentalSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class ExperimentalSettingsFragment : ViewBoundFragment<FragmentExperimentalSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExperimentalSettingsBinding {
        return FragmentExperimentalSettingsBinding.inflate(layoutInflater, container, false)
    }

}