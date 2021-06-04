package org.commonvoice.saverio.ui.dialogs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.BottomsheetGesturesBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateSharedViewModel

class CustomiseGesturesListenDialogFragment : BottomSheetDialogFragment() {

    private val listenViewModel: ListenViewModel by stateSharedViewModel()
    private val mainPrefManager by inject<MainPrefManager>()

    private var _binding: BottomsheetGesturesBinding? = null
    private val binding get() = _binding!!

    private var valueToSave = ""

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
            binding.radioButtonCustomiseGesturesIndicatorSound
        )

        val gesture: Map<String, String> = mapOf(
            "GESTURE_LISTEN_SWIPE_UP" to getString(R.string.text_customise_gestures_settings_swipe_up),
            "GESTURE_LISTEN_SWIPE_DOWN" to getString(R.string.text_customise_gestures_settings_swipe_down),
            "GESTURE_LISTEN_SWIPE_RIGHT" to getString(R.string.text_customise_gestures_settings_swipe_right),
            "GESTURE_LISTEN_SWIPE_LEFT" to getString(R.string.text_customise_gestures_settings_swipe_left),
            "GESTURE_LISTEN_LONG_PRESS" to getString(R.string.text_customise_gestures_settings_long_press),
            "GESTURE_LISTEN_DOUBLE_TAP" to getString(R.string.text_customise_gestures_settings_double_tap)
        )

        binding.titleMainSectionGesture.text =
            getString(R.string.title_customise_gestures_settings_bottomsheet).replace(
                "{{main_section}}",
                getString(R.string.settingsListen)
            ).replace("{{gesture}}", gesture[tag].toString())

        /*
        val reasonsMap: Map<String, String> = mapOf(
            getString(R.string.checkbox_reason1_report_clip) to "offensive-speech",
            getString(R.string.checkbox_reason2_report) to "grammar-or-spelling",
            getString(R.string.checkbox_reason3_report) to "different-language"
        )

        binding.apply {
            val radiobutton: List<CheckBox> = listOf(
                checkBoxReason1Report,
                checkBoxReason2Report,
                checkBoxReason3Report
            )

            titleReportSentenceClip.text = getString(R.string.title_report_clip)
            checkBoxReason1Report.text = getString(R.string.checkbox_reason1_report_clip)
            checkBoxReason4Report.isGone = true

            checkBoxReasonOtherReport.setOnCheckedChangeListener { _, isChecked ->
                textReasonOtherReport.isVisible = isChecked
                if (isChecked) {
                    buttonSendReport.isEnabled = textReasonOtherReport.text.isNotBlank()
                    textReasonOtherReport.requestFocus()
                } else {
                    buttonSendReport.isEnabled = true
                }
            }

            textReasonOtherReport.doAfterTextChanged { editable ->
                if (checkBoxReasonOtherReport.isChecked) {
                    buttonSendReport.isEnabled = editable?.isNotBlank() ?: false
                } else {
                    buttonSendReport.isEnabled = true
                }
            }

            buttonSendReport.onClick {
                val reasons = mutableListOf<String>()

                checkboxes.forEach { checkBox ->
                    if (checkBox.isChecked) {
                        reasonsMap[checkBox.text]?.let { reason ->
                            reasons.add(reason)
                        }
                    }
                }

                if (checkBoxReasonOtherReport.isChecked && textReasonOtherReport.text.isNotBlank()) {
                    reasons.add(textReasonOtherReport.text.toString())
                } else if (checkBoxReasonOtherReport.isChecked) {
                    return@onClick
                }

                if (reasons.isNotEmpty()) {
                    listenViewModel.reportClip(reasons)
                    dismiss()
                }
            }

            buttonCancelReport.onClick {
                dismiss()
            }
        }
        */

        setTheme()
    }

    fun setTheme() = binding.apply {
        /*titleReportSentenceClip.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            20F * mainPrefManager.textSize
        )

        checkBoxReason1Report.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            22F * mainPrefManager.textSize
        )
        checkBoxReason2Report.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            22F * mainPrefManager.textSize
        )
        checkBoxReason3Report.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            22F * mainPrefManager.textSize
        )
        checkBoxReason4Report.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            22F * mainPrefManager.textSize
        )
        checkBoxReasonOtherReport.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            22F * mainPrefManager.textSize
        )
        textReasonOtherReport.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            18F * mainPrefManager.textSize
        )

        buttonCancelReport.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F * mainPrefManager.textSize)
        buttonSendReport.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F * mainPrefManager.textSize)

         */
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}