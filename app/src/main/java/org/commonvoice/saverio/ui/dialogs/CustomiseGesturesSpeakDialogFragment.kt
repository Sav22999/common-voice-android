package org.commonvoice.saverio.ui.dialogs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.BottomsheetGesturesBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.viewmodels.CustomiseGesturesViewModel
import org.koin.android.ext.android.inject

class CustomiseGesturesSpeakDialogFragment : BottomSheetDialogFragment() {

    private val mainPrefManager by inject<MainPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()

    private var _binding: BottomsheetGesturesBinding? = null
    private val binding get() = _binding!!

    private var valueToSave = ""

    private val actionViewModel: CustomiseGesturesViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(CustomiseGesturesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        BottomsheetGesturesBinding.inflate(inflater, container, false).also {
            _binding = it
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioButtons: List<RadioButton> = listOf(
            binding.radioButtonCustomiseGesturesNothing,
            binding.radioButtonCustomiseGesturesGoBack,
            binding.radioButtonCustomiseSkip,
            binding.radioButtonCustomiseGesturesReport,
            binding.radioButtonCustomiseGesturesInfo,
            binding.radioButtonCustomiseGesturesAnimations,
            binding.radioButtonCustomiseGesturesSpeedControl,
            binding.radioButtonCustomiseGesturesSaveRecordings,
            binding.radioButtonCustomiseGesturesSkipConfirmation,
            binding.radioButtonCustomiseGesturesIndicatorSound,
            binding.radioButtonCustomiseGesturesStartStopRecording,
            binding.radioButtonCustomiseGesturesPlayStopRecording
        )

        val gesture: Map<String, String> = mapOf(
            "GESTURE_SPEAK_SWIPE_UP" to getString(R.string.text_customise_gestures_settings_swipe_up),
            "GESTURE_SPEAK_SWIPE_DOWN" to getString(R.string.text_customise_gestures_settings_swipe_down),
            "GESTURE_SPEAK_SWIPE_RIGHT" to getString(R.string.text_customise_gestures_settings_swipe_right),
            "GESTURE_SPEAK_SWIPE_LEFT" to getString(R.string.text_customise_gestures_settings_swipe_left),
            "GESTURE_SPEAK_LONG_PRESS" to getString(R.string.text_customise_gestures_settings_long_press),
            "GESTURE_SPEAK_DOUBLE_TAP" to getString(R.string.text_customise_gestures_settings_double_tap)
        )

        val propertyToUse = when (tag) {
            "GESTURE_SPEAK_SWIPE_UP" -> speakPrefManager.gesturesSwipeTop
            "GESTURE_SPEAK_SWIPE_DOWN" -> speakPrefManager.gesturesSwipeBottom
            "GESTURE_SPEAK_SWIPE_RIGHT" -> speakPrefManager.gesturesSwipeRight
            "GESTURE_SPEAK_SWIPE_LEFT" -> speakPrefManager.gesturesSwipeLeft
            "GESTURE_SPEAK_LONG_PRESS" -> speakPrefManager.gesturesLongPress
            else -> speakPrefManager.gesturesDoubleTap
        }

        binding.apply {
            titleMainSectionGesture.text =
                getString(R.string.title_customise_gestures_settings_bottomsheet).replace(
                    "{{main_section}}",
                    getString(R.string.settingsSpeak)
                ).replace("{{gesture}}", gesture[tag].toString())

            buttonCancelCustomiseGestures.onClick {
                dismiss()
            }

            radioButtons.forEach { radioButton ->
                radioButton.isGone = false
            }
            radioButtonCustomiseSkip.text =
                getString(R.string.text_customise_gestures_settings_skip_sentence)
            radioButtonCustomiseGesturesReport.text =
                getString(R.string.text_customise_gestures_settings_report_sentence)
            radioButtonCustomiseGesturesInfo.text =
                getString(R.string.text_customise_gestures_settings_show_info_sentence)
            radioButtonCustomiseGesturesAnimations.text =
                getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_animations)
                )
            radioButtonCustomiseGesturesSpeedControl.text =
                getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_show_speed_control_in_speak_listen)
                )
            radioButtonCustomiseGesturesSaveRecordings.text =
                getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.switch_settings_save_recordings_on_device)
                )
            radioButtonCustomiseGesturesSkipConfirmation.text =
                getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_skip_recording_confirmation)
                )
            radioButtonCustomiseGesturesIndicatorSound.text =
                getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_recording_indicator_sound)
                )
            radioGroupCustomiseGestures.check(
                when (propertyToUse) {
                    "back" -> R.id.radioButtonCustomiseGesturesGoBack
                    "skip" -> R.id.radioButtonCustomiseSkip
                    "report" -> R.id.radioButtonCustomiseGesturesReport
                    "info" -> R.id.radioButtonCustomiseGesturesInfo
                    "animations" -> R.id.radioButtonCustomiseGesturesAnimations
                    "speed-control" -> R.id.radioButtonCustomiseGesturesSpeedControl
                    "save-recordings" -> R.id.radioButtonCustomiseGesturesSaveRecordings
                    "skip-confirmation" -> R.id.radioButtonCustomiseGesturesSkipConfirmation
                    "indicator-sound" -> R.id.radioButtonCustomiseGesturesIndicatorSound
                    "start-stop-recording" -> R.id.radioButtonCustomiseGesturesStartStopRecording
                    "play-stop-recording" -> R.id.radioButtonCustomiseGesturesPlayStopRecording
                    else -> R.id.radioButtonCustomiseGesturesNothing
                }
            )
            valueToSave = propertyToUse
            radioGroupCustomiseGestures.setOnCheckedChangeListener { _, checkedId ->
                valueToSave = when (checkedId) {
                    R.id.radioButtonCustomiseGesturesNothing -> ""
                    R.id.radioButtonCustomiseGesturesGoBack -> "back"
                    R.id.radioButtonCustomiseSkip -> "skip"
                    R.id.radioButtonCustomiseGesturesReport -> "report"
                    R.id.radioButtonCustomiseGesturesInfo -> "info"
                    R.id.radioButtonCustomiseGesturesAnimations -> "animations"
                    R.id.radioButtonCustomiseGesturesSpeedControl -> "speed-control"
                    R.id.radioButtonCustomiseGesturesSaveRecordings -> "save-recordings"
                    R.id.radioButtonCustomiseGesturesSkipConfirmation -> "skip-confirmation"
                    R.id.radioButtonCustomiseGesturesIndicatorSound -> "indicator-sound"
                    R.id.radioButtonCustomiseGesturesStartStopRecording -> "start-stop-recording"
                    R.id.radioButtonCustomiseGesturesPlayStopRecording -> "play-stop-recording"
                    else -> ""
                }
                save()
            }

            buttonSaveCustomiseGestures.onClick {
                save()
                dismiss()
            }
        }

        setTheme()
    }

    fun save() {
        when (tag) {
            "GESTURE_SPEAK_SWIPE_UP" -> speakPrefManager.gesturesSwipeTop = valueToSave
            "GESTURE_SPEAK_SWIPE_DOWN" -> speakPrefManager.gesturesSwipeBottom = valueToSave
            "GESTURE_SPEAK_SWIPE_RIGHT" -> speakPrefManager.gesturesSwipeRight = valueToSave
            "GESTURE_SPEAK_SWIPE_LEFT" -> speakPrefManager.gesturesSwipeLeft = valueToSave
            "GESTURE_SPEAK_LONG_PRESS" -> speakPrefManager.gesturesLongPress = valueToSave
            else -> speakPrefManager.gesturesDoubleTap = valueToSave
        }
        actionViewModel.changeAction(valueToSave)
    }

    fun setTheme() = binding.apply {
        titleMainSectionGesture.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            15F * mainPrefManager.textSize
        )
        titleChooseAction.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            20F * mainPrefManager.textSize
        )

        buttonCancelCustomiseGestures.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            16F * mainPrefManager.textSize
        )
        buttonSaveCustomiseGestures.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            16F * mainPrefManager.textSize
        )
    }

    override fun onDestroyView() {
        _binding = null
        actionViewModel.changeAction("")
        super.onDestroyView()
    }
}