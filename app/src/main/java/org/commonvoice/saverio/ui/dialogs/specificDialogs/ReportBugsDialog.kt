package org.commonvoice.saverio.ui.dialogs.specificDialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogReportBugsBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager

class ReportBugsDialog(
    private val context: Activity,
    private val mainPrefManager: MainPrefManager,
) : CustomDialogInterface<DialogReportBugsBinding>(
    makeBackgroundTransparent = true
) {

    override fun render(inflater: LayoutInflater): DialogReportBugsBinding {
        return DialogReportBugsBinding.inflate(inflater).also {
            it.btnOkMessageDialogReportBug.onClick {
                mainPrefManager.showReportWebsiteBugs = !it.checkDoNotShowAnymoreReportBug.isChecked
                dismiss()
            }

            it.btnMessageDialogReportBugMozillaDiscourse.onClick {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://mzl.la/3f7sHqj")
                    )
                )
                dismiss()
            }

            it.btnMessageDialogReportBugGitHub.onClick {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://bit.ly/2Z73TZZ")
                    )
                )
                dismiss()
            }
        }
    }

}