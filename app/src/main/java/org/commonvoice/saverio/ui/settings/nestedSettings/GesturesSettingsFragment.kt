package org.commonvoice.saverio.ui.settings.nestedSettings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.commonvoice.saverio.databinding.FragmentGesturesSettingsBinding
import org.commonvoice.saverio.ui.dialogs.CustomiseGesturesListenDialogFragment
import org.commonvoice.saverio.ui.dialogs.CustomiseGesturesSpeakDialogFragment
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.koin.android.ext.android.inject
import org.commonvoice.saverio.R
import org.commonvoice.saverio_lib.viewmodels.CustomiseGesturesViewModel
import java.util.stream.Collector.of

class GesturesSettingsFragment : ViewBoundFragment<FragmentGesturesSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGesturesSettingsBinding {
        return FragmentGesturesSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()
    private val listenPrefManager by inject<ListenPrefManager>()
    private val actionViewModel: CustomiseGesturesViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(CustomiseGesturesViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionViewModel.action.observe(this, Observer { action ->
            updateAllGestures()
        })
    }

    override fun onStart() {
        super.onStart()

        withBinding {
            buttonBackSettingsSubSectionGestures.setOnClickListener {
                activity?.onBackPressed()
            }

            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsGestures.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

            switchSettingsSubSectionGestures.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areGesturesEnabled = isChecked
                settingsSectionGesturesSubSpeak.isGone = !isChecked
                settingsSectionGesturesSubListen.isGone = !isChecked

                if (isChecked) {
                    nestedScrollSettingsGestures.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }
                } else {
                    nestedScrollSettingsGestures.setupOnSwipeRight(requireContext()) { }
                }
            }
            switchSettingsSubSectionGestures.isChecked = mainPrefManager.areGesturesEnabled
            settingsSectionGesturesSubSpeak.isGone = !mainPrefManager.areGesturesEnabled
            settingsSectionGesturesSubListen.isGone = !mainPrefManager.areGesturesEnabled

            buttonGesturesLearnMore.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.saveriomorelli.com/commonvoice/gestures/")))
            }

            updateAllGestures()

            buttonGesturesSpeakSwipeRight.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_RIGHT"
                )
            }
            buttonGesturesSpeakSwipeLeft.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_LEFT"
                )
            }
            buttonGesturesSpeakSwipeUp.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_UP"
                )
            }
            buttonGesturesSpeakSwipeDown.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_SWIPE_DOWN"
                )
            }
            buttonGesturesSpeakLongPress.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_LONG_PRESS"
                )
            }
            buttonGesturesSpeakDoubleTap.setOnClickListener {
                CustomiseGesturesSpeakDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_SPEAK_DOUBLE_TAP"
                )
            }

            buttonGesturesListenSwipeRight.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_RIGHT"
                )
            }
            buttonGesturesListenSwipeLeft.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_LEFT"
                )
            }
            buttonGesturesListenSwipeUp.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_UP"
                )
            }
            buttonGesturesListenSwipeDown.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_SWIPE_DOWN"
                )
            }
            buttonGesturesListenLongPress.setOnClickListener {
                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_LONG_PRESS"
                )
            }
            buttonGesturesListenDoubleTap.setOnClickListener {

                CustomiseGesturesListenDialogFragment().show(
                    activity?.supportFragmentManager!!,
                    "GESTURE_LISTEN_DOUBLE_TAP"
                )
            }
        }

        setTheme()
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsGestures)

            theme.setElements(requireContext(), settingsSectionGesturesSubSpeak)
            theme.setElements(requireContext(), settingsSectionGesturesSubListen)
            theme.setElements(requireContext(), settingsSectionGestures)

            theme.setElement(requireContext(), 3, settingsSectionGesturesSubSpeak)
            theme.setElement(requireContext(), 3, settingsSectionGesturesSubListen)
            theme.setElement(requireContext(), 3, settingsSectionGestures)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionGestures, textSize = 20F)
        }
    }

    public fun test() {
        println("!! !! test")
    }

    private fun getTheValue(action: String = "", section: String): String {
        return if (section == "speak") {
            //section=speak
            when (action) {
                "back" -> getString(R.string.text_customise_gestures_settings_go_back)
                "skip" -> getString(R.string.text_customise_gestures_settings_skip_sentence)
                "report" -> getString(R.string.text_customise_gestures_settings_report_sentence)
                "info" -> getString(R.string.text_customise_gestures_settings_show_info_sentence)
                "animations" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_animations)
                )
                "speed-control" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_show_speed_control_in_speak_listen)
                )
                "save-recordings" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.switch_settings_save_recordings_on_device)
                )
                "skip-confirmation" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_skip_recording_confirmation)
                )
                "indicator-sound" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_recording_indicator_sound)
                )
                else -> ""
            }
        } else {
            //section=listen
            when (action) {
                "back" -> getString(R.string.text_customise_gestures_settings_go_back)
                "skip" -> getString(R.string.text_customise_gestures_settings_skip_clip)
                "report" -> getString(R.string.text_customise_gestures_settings_report_clip)
                "info" -> getString(R.string.text_customise_gestures_settings_show_info_clip)
                "animations" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_animations)
                )
                "speed-control" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_show_speed_control_in_speak_listen)
                )
                "validate-yes" -> getString(R.string.text_customise_gestures_settings_accept_clip)
                "validate-no" -> getString(R.string.text_customise_gestures_settings_reject_clip)
                "auto-play" -> getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_autoplay_clips_after_loading_settings)
                )
                else -> ""
            }
        }
    }

    private fun updateAllGestures() {
        withBinding {
            //speak
            buttonGesturesSpeakSwipeRight.text = if (speakPrefManager.gesturesSwipeRight != "") {
                getString(R.string.text_customise_gestures_settings_swipe_right_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = speakPrefManager.gesturesSwipeRight, section = "speak")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_right)
            }

            buttonGesturesSpeakSwipeLeft.text = if (speakPrefManager.gesturesSwipeLeft != "") {
                getString(R.string.text_customise_gestures_settings_swipe_left_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = speakPrefManager.gesturesSwipeLeft, section = "speak")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_left)
            }

            buttonGesturesSpeakSwipeUp.text = if (speakPrefManager.gesturesSwipeTop != "") {
                getString(R.string.text_customise_gestures_settings_swipe_up_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = speakPrefManager.gesturesSwipeTop, section = "speak")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_up)
            }

            buttonGesturesSpeakSwipeDown.text = if (speakPrefManager.gesturesSwipeBottom != "") {
                getString(R.string.text_customise_gestures_settings_swipe_down_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = speakPrefManager.gesturesSwipeBottom, section = "speak")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_down)
            }

            buttonGesturesSpeakLongPress.text = if (speakPrefManager.gesturesLongPress != "") {
                getString(R.string.text_customise_gestures_settings_long_press_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = speakPrefManager.gesturesLongPress, section = "speak")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_long_press)
            }

            buttonGesturesSpeakDoubleTap.text = if (speakPrefManager.gesturesDoubleTap != "") {
                getString(R.string.text_customise_gestures_settings_double_tap_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = speakPrefManager.gesturesDoubleTap, section = "speak")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_double_tap)
            }

            //listen
            buttonGesturesListenSwipeRight.text = if (listenPrefManager.gesturesSwipeRight != "") {
                getString(R.string.text_customise_gestures_settings_swipe_right_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = listenPrefManager.gesturesSwipeRight, section = "listen")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_right)
            }

            buttonGesturesListenSwipeLeft.text = if (listenPrefManager.gesturesSwipeLeft != "") {
                getString(R.string.text_customise_gestures_settings_swipe_left_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = listenPrefManager.gesturesSwipeLeft, section = "listen")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_left)
            }

            buttonGesturesListenSwipeUp.text = if (listenPrefManager.gesturesSwipeTop != "") {
                getString(R.string.text_customise_gestures_settings_swipe_up_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = listenPrefManager.gesturesSwipeTop, section = "listen")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_up)
            }

            buttonGesturesListenSwipeDown.text = if (listenPrefManager.gesturesSwipeBottom != "") {
                getString(R.string.text_customise_gestures_settings_swipe_down_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = listenPrefManager.gesturesSwipeBottom, section = "listen")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_swipe_down)
            }

            buttonGesturesListenLongPress.text = if (listenPrefManager.gesturesLongPress != "") {
                getString(R.string.text_customise_gestures_settings_long_press_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = listenPrefManager.gesturesLongPress, section = "listen")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_long_press)
            }

            buttonGesturesListenDoubleTap.text = if (listenPrefManager.gesturesDoubleTap != "") {
                getString(R.string.text_customise_gestures_settings_double_tap_enabled).replace(
                    "{{feature_enabled}}",
                    getTheValue(action = listenPrefManager.gesturesDoubleTap, section = "listen")
                )
            } else {
                getString(R.string.text_customise_gestures_settings_double_tap)
            }
        }
    }
}