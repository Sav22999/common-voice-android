package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentUiSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class UISettingsFragment : ViewBoundFragment<FragmentUiSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUiSettingsBinding {
        return FragmentUiSettingsBinding.inflate(layoutInflater, container, false)
    }

}