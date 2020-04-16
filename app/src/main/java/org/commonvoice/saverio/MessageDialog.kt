package org.commonvoice.saverio

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.message_dialog.view.*

class MessageDialog(
    private val context: Context,
    private val message_type: Int,
    private val message_title: String,
    private val message_text: String,
    private val details: String = "") {

    constructor(context: Context, title: String, text: String) : this(context,  0, title, text) // 0->standard (Ok)

    fun show() {
        try {
            val dialogView =
                LayoutInflater.from(this.context).inflate(R.layout.message_dialog, null)
            val builder = AlertDialog.Builder(this.context)
                .setView(dialogView)
                .setTitle(message_title)
            //show dialog
            val alerDialog = builder.show()
            dialogView.labelTextMessageDialog.text = this.message_text
            if (this.details != "") {
                dialogView.labelDetailsMessageDialog.text = this.details
                dialogView.btnShowHideDetailsMessageDialog.isGone = false
                dialogView.btnShowHideDetailsMessageDialog.paintFlags =
                    Paint.UNDERLINE_TEXT_FLAG
                dialogView.btnShowHideDetailsMessageDialog.text = "Show details"
                dialogView.btnShowHideDetailsMessageDialog.setOnClickListener {
                    if (!dialogView.labelDetailsMessageDialog.isGone) {
                        dialogView.btnShowHideDetailsMessageDialog.text = "Show details"
                        dialogView.labelDetailsMessageDialog.isGone = true
                    } else {
                        dialogView.btnShowHideDetailsMessageDialog.text = "Hide details"
                        dialogView.labelDetailsMessageDialog.isGone = false
                    }
                }
            }
            dialogView.btnOkMessageDialog.setOnClickListener {
                //dismiss dialog
                alerDialog.dismiss()
            }
        } catch (exception: Exception) {
            println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
        }
    }
}