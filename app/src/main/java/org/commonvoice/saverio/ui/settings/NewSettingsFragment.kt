package org.commonvoice.saverio.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentNewSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick

class NewSettingsFragment : ViewBoundFragment<FragmentNewSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNewSettingsBinding {
        return FragmentNewSettingsBinding.inflate(layoutInflater, container, false)
    }

    override fun onStart() {
        super.onStart()

        withBinding {
            settingsSectionAdvanced.onClick {
                findNavController().navigate(R.id.advancedSettingsFragment)
            }

            settingsSectionApp.onClick {
                findNavController().navigate(R.id.appSettingsFragment)
            }

            settingsSectionExperimentalFeatures.onClick {
                findNavController().navigate(R.id.experimentalSettingsFragment)
            }

            settingsSectionGestures.onClick {
                findNavController().navigate(R.id.gesturesSettingsFragment)
            }

            settingsSectionListen.onClick {
                findNavController().navigate(R.id.listenSettingsFragment)
            }

            settingsSectionSpeak.onClick {
                findNavController().navigate(R.id.speakSettingsFragment)
            }

            settingsSectionUI.onClick {
                findNavController().navigate(R.id.UISettingsFragment)
            }

            settingsSectionOfflineMode.onClick {
                findNavController().navigate(R.id.offlineModeSettingsFragment)
            }
        }
    }

}