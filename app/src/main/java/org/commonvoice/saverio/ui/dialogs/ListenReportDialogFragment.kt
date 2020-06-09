package org.commonvoice.saverio.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_report.*
import org.commonvoice.saverio.R
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio.utils.sharedStateViewModel
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel

class ListenReportDialogFragment : BottomSheetDialogFragment() {

    private val listenViewModel: ListenViewModel by sharedStateViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reasonsMap: Map<String, String> by lazy {
            mapOf(
                getString(R.string.checkbox_reason1_report_clip) to "offensive-speech",
                getString(R.string.checkbox_reason2_report) to "grammar-or-spelling",
                getString(R.string.checkbox_reason3_report) to "different-language"
            )
        }

        val checkboxes: List<CheckBox> by lazy {
            listOf(
                checkBoxReason1Report,
                checkBoxReason2Report,
                checkBoxReason3Report
            )
        }

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
                this.dismiss()
            }
        }

        buttonCancelReport.onClick {
            this.dismiss()
        }
    }

}