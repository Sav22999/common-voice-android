package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentOfflineSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class OfflineModeSettingsFragment : ViewBoundFragment<FragmentOfflineSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOfflineSettingsBinding {
        return FragmentOfflineSettingsBinding.inflate(layoutInflater, container, false)
    }

}