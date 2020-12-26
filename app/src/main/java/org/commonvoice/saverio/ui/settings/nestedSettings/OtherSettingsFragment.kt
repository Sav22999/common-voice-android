package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_other_settings.*
import org.commonvoice.saverio.databinding.FragmentOtherSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.koin.android.ext.android.inject

class OtherSettingsFragment : ViewBoundFragment<FragmentOtherSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOtherSettingsBinding {
        return FragmentOtherSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val settingsPrefManager by inject<SettingsPrefManager>()

    override fun onStart() {
        super.onStart()

        buttonBackSettingsSubSectionOther.setOnClickListener {
            activity?.onBackPressed()
        }

        withBinding {
            nestedScrollSettingsOther.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            switchCheckUpdates.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.automaticallyCheckForUpdates = isChecked
            }
            switchCheckUpdates.isChecked = settingsPrefManager.automaticallyCheckForUpdates
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsOther)

            theme.setElements(requireContext(), settingsSubSectionOther)

            theme.setElement(requireContext(), 3, settingsSubSectionOther)
        }
    }
}