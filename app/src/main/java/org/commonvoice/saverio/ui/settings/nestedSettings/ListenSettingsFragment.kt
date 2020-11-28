package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentListenSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class ListenSettingsFragment : ViewBoundFragment<FragmentListenSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListenSettingsBinding {
        return FragmentListenSettingsBinding.inflate(layoutInflater, container, false)
    }

}