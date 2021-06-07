package org.commonvoice.saverio.ui.dialogs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.BottomsheetGesturesBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.viewmodels.CustomiseGesturesViewModel
import org.koin.android.ext.android.inject

class CustomiseGesturesListenDialogFragment : BottomSheetDialogFragment() {

    private val mainPrefManager by inject<MainPrefManager>()
    private val listenPrefManager by inject<ListenPrefManager>()

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
            binding.radioButtonCustomiseGesturesAutoPlay,
            binding.radioButtonCustomiseGesturesValidateYes,
            binding.radioButtonCustomiseGesturesValidateNo
        )

        val gesture: Map<String, String> = mapOf(
            "GESTURE_LISTEN_SWIPE_UP" to getString(R.string.text_customise_gestures_settings_swipe_up),
            "GESTURE_LISTEN_SWIPE_DOWN" to getString(R.string.text_customise_gestures_settings_swipe_down),
            "GESTURE_LISTEN_SWIPE_RIGHT" to getString(R.string.text_customise_gestures_settings_swipe_right),
            "GESTURE_LISTEN_SWIPE_LEFT" to getString(R.string.text_customise_gestures_settings_swipe_left),
            "GESTURE_LISTEN_LONG_PRESS" to getString(R.string.text_customise_gestures_settings_long_press),
            "GESTURE_LISTEN_DOUBLE_TAP" to getString(R.string.text_customise_gestures_settings_double_tap)
        )

        val propertyToUse = when (tag) {
            "GESTURE_LISTEN_SWIPE_UP" -> listenPrefManager.gesturesSwipeTop
            "GESTURE_LISTEN_SWIPE_DOWN" -> listenPrefManager.gesturesSwipeBottom
            "GESTURE_LISTEN_SWIPE_RIGHT" -> listenPrefManager.gesturesSwipeRight
            "GESTURE_LISTEN_SWIPE_LEFT" -> listenPrefManager.gesturesSwipeLeft
            "GESTURE_LISTEN_LONG_PRESS" -> listenPrefManager.gesturesLongPress
            else -> listenPrefManager.gesturesDoubleTap
        }

        binding.apply {
            titleMainSectionGesture.text =
                getString(R.string.title_customise_gestures_settings_bottomsheet).replace(
                    "{{main_section}}",
                    getString(R.string.settingsListen)
                ).replace("{{gesture}}", gesture[tag].toString())

            buttonCancelCustomiseGestures.onClick {
                dismiss()
            }

            radioButtons.forEach { radioButton ->
                radioButton.isGone = false
            }
            radioButtonCustomiseSkip.text =
                getString(R.string.text_customise_gestures_settings_skip_clip)
            radioButtonCustomiseGesturesReport.text =
                getString(R.string.text_customise_gestures_settings_report_clip)
            radioButtonCustomiseGesturesInfo.text =
                getString(R.string.text_customise_gestures_settings_show_info_clip)
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
            radioButtonCustomiseGesturesAutoPlay.text =
                getString(R.string.text_customise_gestures_settings_enable_disable_feature).replace(
                    "{{feature}}",
                    getString(R.string.txt_autoplay_clips_after_loading_settings)
                )

            radioGroupCustomiseGestures.check(
                when (propertyToUse) {
                    "back" -> R.id.radioButtonCustomiseGesturesGoBack
                    "skip" -> R.id.radioButtonCustomiseSkip
                    "report" -> R.id.radioButtonCustomiseGesturesReport
                    "info" -> R.id.radioButtonCustomiseGesturesInfo
                    "animations" -> R.id.radioButtonCustomiseGesturesAnimations
                    "speed-control" -> R.id.radioButtonCustomiseGesturesSpeedControl
                    "auto-play" -> R.id.radioButtonCustomiseGesturesAutoPlay
                    "validate-yes" -> R.id.radioButtonCustomiseGesturesValidateYes
                    "validate-no" -> R.id.radioButtonCustomiseGesturesValidateNo
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
                    R.id.radioButtonCustomiseGesturesAutoPlay -> "auto-play"
                    R.id.radioButtonCustomiseGesturesValidateYes -> "validate-yes"
                    R.id.radioButtonCustomiseGesturesValidateNo -> "validate-no"
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
            "GESTURE_LISTEN_SWIPE_UP" -> listenPrefManager.gesturesSwipeTop = valueToSave
            "GESTURE_LISTEN_SWIPE_DOWN" -> listenPrefManager.gesturesSwipeBottom =
                valueToSave
            "GESTURE_LISTEN_SWIPE_RIGHT" -> listenPrefManager.gesturesSwipeRight =
                valueToSave
            "GESTURE_LISTEN_SWIPE_LEFT" -> listenPrefManager.gesturesSwipeLeft = valueToSave
            "GESTURE_LISTEN_LONG_PRESS" -> listenPrefManager.gesturesLongPress = valueToSave
            else -> listenPrefManager.gesturesDoubleTap = valueToSave
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