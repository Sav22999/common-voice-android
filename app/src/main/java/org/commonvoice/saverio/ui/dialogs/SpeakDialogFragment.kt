package org.commonvoice.saverio.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_report_speak.*
import org.commonvoice.saverio.R
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio.utils.sharedStateViewModel
import org.commonvoice.saverio_lib.viewmodels.SpeakViewModel

class SpeakDialogFragment: BottomSheetDialogFragment() {

    private val speakViewModel: SpeakViewModel by sharedStateViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_report_speak, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonSendReport.onClick {
            speakViewModel.reportSentence(listOf("Prova", "Prova2"))
            this.dismiss()
        }

        buttonCancelReport.onClick {
            this.dismiss()
        }
    }

}