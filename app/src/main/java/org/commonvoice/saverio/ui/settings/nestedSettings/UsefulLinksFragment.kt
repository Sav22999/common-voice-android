package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentUsefulLinksSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class UsefulLinksFragment : ViewBoundFragment<FragmentUsefulLinksSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUsefulLinksSettingsBinding {
        return FragmentUsefulLinksSettingsBinding.inflate(layoutInflater, container, false)
    }

}