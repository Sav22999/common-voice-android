package org.commonvoice.saverio.ui.dialogs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.BottomsheetReportBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateSharedViewModel

class SpeakReportDialogFragment : BottomSheetDialogFragment() {

    private val speakViewModel: SpeakViewModel by stateSharedViewModel()
    private val mainPrefManager by inject<MainPrefManager>()

    private var _binding: BottomsheetReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        BottomsheetReportBinding.inflate(inflater, container, false).also {
            _binding = it
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reasonsMap: Map<String, String> = mapOf(
            getString(R.string.checkbox_reason1_report_sentence) to "offensive-language",
            getString(R.string.checkbox_reason2_report) to "grammar-or-spelling",
            getString(R.string.checkbox_reason3_report) to "different-language",
            getString(R.string.checkbox_reason4_report) to "difficult-pronunciation"
        )

        binding.apply {
            val checkboxes: List<CheckBox> = listOf(
                checkBoxReason1Report,
                checkBoxReason2Report,
                checkBoxReason3Report,
                checkBoxReason4Report
            )

            titleReportSentenceClip.text = getString(R.string.title_report_sentence)
            checkBoxReason1Report.text = getString(R.string.checkbox_reason1_report_sentence)
            checkBoxReason4Report.isGone = false

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
                    speakViewModel.reportSentence(reasons)
                    dismiss()
                }
            }

            buttonCancelReport.onClick {
                dismiss()
            }
        }

        setTheme()
    }

    fun setTheme() = binding.apply {
        titleReportSentenceClip.setTextSize(
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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}