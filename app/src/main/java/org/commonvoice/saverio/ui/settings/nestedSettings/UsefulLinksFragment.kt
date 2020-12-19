package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentUsefulLinkSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class UsefulLinksFragment : ViewBoundFragment<FragmentUsefulLinkSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUsefulLinkSettingsBinding {
        return FragmentUsefulLinkSettingsBinding.inflate(layoutInflater, container, false)
    }

}