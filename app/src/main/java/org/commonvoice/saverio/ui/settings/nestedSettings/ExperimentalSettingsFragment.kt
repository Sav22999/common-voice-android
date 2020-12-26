package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_experimental_settings.*
import org.commonvoice.saverio.databinding.FragmentExperimentalSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment

class ExperimentalSettingsFragment : ViewBoundFragment<FragmentExperimentalSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExperimentalSettingsBinding {
        return FragmentExperimentalSettingsBinding.inflate(layoutInflater, container, false)
    }

    override fun onStart() {
        super.onStart()

        buttonBackSettingsSubSectionExperimental.setOnClickListener {
            activity?.onBackPressed()
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsExperimentalFeatures)

            theme.setElements(requireContext(), settingsSectionExperimental)

            theme.setElement(requireContext(), 3, settingsSectionExperimental)
        }
    }
}