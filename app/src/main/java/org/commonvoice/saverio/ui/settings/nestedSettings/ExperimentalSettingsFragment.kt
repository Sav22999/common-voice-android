package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.databinding.FragmentExperimentalSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject

class ExperimentalSettingsFragment : ViewBoundFragment<FragmentExperimentalSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExperimentalSettingsBinding {
        return FragmentExperimentalSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()

    override fun onStart() {
        super.onStart()

        binding.buttonBackSettingsSubSectionExperimental.setOnClickListener {
            activity?.onBackPressed()
        }

        if (mainPrefManager.areGesturesEnabled)
            binding.nestedScrollSettingsExperimentalFeatures.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsExperimentalFeatures)

            theme.setElements(requireContext(), settingsSectionExperimental)

            theme.setElement(requireContext(), 3, settingsSectionExperimental)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionExperimental, textSize = 20F)
        }
    }
}