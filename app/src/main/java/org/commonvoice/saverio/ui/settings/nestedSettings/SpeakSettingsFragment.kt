package org.commonvoice.saverio.ui.settings.nestedSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import org.commonvoice.saverio.GenericViewModel
import org.commonvoice.saverio.databinding.FragmentSpeakSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.koin.android.ext.android.inject

class SpeakSettingsFragment : ViewBoundFragment<FragmentSpeakSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSpeakSettingsBinding {
        return FragmentSpeakSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()

    override fun onStart() {
        super.onStart()

        withBinding {
            val viewModel = activity?.run {
                ViewModelProviders.of(this).get(GenericViewModel::class.java)
            } ?: throw Exception("?? Invalid Activity ??")
            if (viewModel.fromFragment.value != "settings") activity?.onBackPressed()

            buttonBackSettingsSubSectionSpeak.setOnClickListener {
                activity?.onBackPressed()
            }

            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsSpeak.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            switchSoundIndicator.setOnCheckedChangeListener { _, isChecked ->
                speakPrefManager.playRecordingSoundIndicator = isChecked
            }
            switchSoundIndicator.isChecked = speakPrefManager.playRecordingSoundIndicator

            switchSkipConfirmation.setOnCheckedChangeListener { _, isChecked ->
                speakPrefManager.skipRecordingConfirmation = isChecked
            }
            switchSkipConfirmation.isChecked = speakPrefManager.skipRecordingConfirmation

            switchSaveRecordingsOnDevice.setOnCheckedChangeListener { _, isChecked ->
                speakPrefManager.saveRecordingsOnDevice = isChecked
            }
            switchSaveRecordingsOnDevice.isChecked = speakPrefManager.saveRecordingsOnDevice

            switchShowSpeedControlSpeak.setOnCheckedChangeListener { _, isChecked ->
                speakPrefManager.showSpeedControl = isChecked
                if (!isChecked) {
                    speakPrefManager.audioSpeed = 1F
                }
            }
            switchShowSpeedControlSpeak.isChecked = speakPrefManager.showSpeedControl
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsSpeak)

            theme.setElements(requireContext(), settingsSectionSpeak)

            theme.setElement(requireContext(), 3, settingsSectionSpeak)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionSpeak, textSize = 20F)
        }
    }
}
