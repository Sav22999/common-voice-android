package org.commonvoice.saverio

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.message_dialog.view.*

class MessageDialog {
    private var message_type: Int = 0 // 0->standard (Ok)
    private var message_text: String = ""
    private var message_title: String = ""
    private var message_details: String = ""
    private var context: Context? = null;

    constructor(context: Context, title: String, text: String) {
        this.context = context
        message_type = 0
        this.message_title = title
        this.message_text = text
    }

    constructor(context: Context, type: Int, title: String, text: String, details: String = "") {
        this.context = context
        this.message_type = type
        this.message_title = title
        this.message_text = text
        this.message_details = details
    }

    fun show() {
        if (this.context != null) {
            val dialogView =
                LayoutInflater.from(this.context).inflate(R.layout.message_dialog, null)
            val builder = AlertDialog.Builder(this.context!!)
                .setView(dialogView)
                .setTitle(message_title)
            //show dialog
            val alerDialog = builder.show()
            dialogView.labelTextMessageDialog.setText(this.message_text)
            if (this.message_details != "") {
                dialogView.labelDetailsMessageDialog.setText(this.message_details)
                dialogView.btnShowHideDetailsMessageDialog.isGone = false
                dialogView.btnShowHideDetailsMessageDialog.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                dialogView.btnShowHideDetailsMessageDialog.setText("Show details")
                dialogView.btnShowHideDetailsMessageDialog.setOnClickListener {
                    if (!dialogView.labelDetailsMessageDialog.isGone) {
                        dialogView.btnShowHideDetailsMessageDialog.setText("Show details")
                        dialogView.labelDetailsMessageDialog.isGone = true
                    } else {
                        dialogView.btnShowHideDetailsMessageDialog.setText("Hide details")
                        dialogView.labelDetailsMessageDialog.isGone = false
                    }
                }
            }
            dialogView.btnOkMessageDialog.setOnClickListener {
                //dismiss dialog
                alerDialog.dismiss()
            }
        }
    }
}