package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentAppSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class AppSettingsFragment : ViewBoundFragment<FragmentAppSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAppSettingsBinding {
        return FragmentAppSettingsBinding.inflate(layoutInflater, container, false)
    }

}