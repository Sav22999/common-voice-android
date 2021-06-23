package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentExperimentalSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.koin.android.ext.android.inject

class ExperimentalSettingsFragment : ViewBoundFragment<FragmentExperimentalSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExperimentalSettingsBinding {
        return FragmentExperimentalSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()

    override fun onStart() {
        super.onStart()

        binding.buttonBackSettingsSubSectionExperimental.setOnClickListener {
            activity?.onBackPressed()
        }

        withBinding {
            textSettingsExperimentalFeaturesSpeakAndListen.text =
                getString(R.string.txt_speak_and_listen).replace(
                    "{{listen_name}}",
                    getString(R.string.settingsListen)
                ).replace("{{speak_name}}", getString(R.string.settingsSpeak))

            switchThemeLightAlsoForSentenceBox.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.isLightThemeSentenceBoxSpeakListen = isChecked
            }
            switchThemeLightAlsoForSentenceBox.isChecked =
                settingsPrefManager.isLightThemeSentenceBoxSpeakListen

            switchShowInfoIconSpeakListen.setOnCheckedChangeListener { _, isChecked ->
                settingsPrefManager.showInfoIcon = isChecked
            }
            switchShowInfoIconSpeakListen.isChecked =
                settingsPrefManager.showInfoIcon

            switchPushToTalkSpeak.setOnCheckedChangeListener { _, isChecked ->
                speakPrefManager.pushToTalk = isChecked
            }
            switchShowInfoIconSpeakListen.isChecked = speakPrefManager.pushToTalk
        }

        if (mainPrefManager.areGesturesEnabled)
            binding.nestedScrollSettingsExperimentalFeatures.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsExperimentalFeatures)

            theme.setElements(requireContext(), settingsSectionExperimental)
            theme.setElements(requireContext(), settingsSectionSpeakAndListenExperimental)
            theme.setElements(requireContext(), settingsSectionSpeakExperimental)

            theme.setElement(requireContext(), 3, settingsSectionExperimental)
            theme.setElement(requireContext(), 3, settingsSectionSpeakAndListenExperimental)
            theme.setElement(requireContext(), 3, settingsSectionSpeakExperimental)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionExperimental, textSize = 20F)
        }
    }
}