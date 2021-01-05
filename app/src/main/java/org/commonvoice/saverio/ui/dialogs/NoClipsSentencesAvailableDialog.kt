package org.commonvoice.saverio.ui.dialogs

import android.content.Context
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio.R
import org.commonvoice.saverio.utils.*

class NoClipsSentencesAvailableDialog(
    private val ctx: Context,
    private val isSentencesDialog: Boolean,
    private val count: Int,
    private val darkLightTheme: DarkLightTheme
) {

    fun show(onDismiss: (() -> Unit)? = null) {
        AlertDialog(
            ctx,
            layout = R.layout.message_dialog_no_clips_sentences_available
        ).setupViews {
            makeRootViewTransparent()
            withTextView(R.id.labelTextMessageDialog, textLiteral = getText(ctx))
            withButton(R.id.btnOkMessageDialog) {
                this.dismiss()
            }
            setOnDismissListener {
                if (onDismiss != null) {
                    onDismiss()
                }
            }
            setTheme(this)
        }
    }

    private fun getText(ctx: Context): String {
        return when {
            isSentencesDialog && count == 0 -> {
                ctx.getString(R.string.txt_sentences_finished_offline_mode)
            }
            !isSentencesDialog && count == 0 -> {
                ctx.getString(R.string.txt_clips_finished_offline_mode)
            }
            isSentencesDialog -> {
                ctx.getString(R.string.txt_residual_sentences_offline_mode)
                    .replace("{{*{{n_sentences}}*}}", "$count")
            }
            !isSentencesDialog -> {
                ctx.getString(R.string.txt_residual_clips_offline_mode)
                    .replace("{{*{{n_clips}}*}}", "$count")
            }
            else -> { //this will never happen
                ctx.getString(R.string.messageDialogErrorTitle)
            }
        }
    }

    fun setTheme(dialog: AlertDialog) {
        darkLightTheme.setElement(
            dialog.getView(R.id.messageDialogSectionMiddle)
        )
        darkLightTheme.setElement(
            ctx,
            -1,
            dialog.getView(R.id.messageDialogSectionMiddle)
        )
        darkLightTheme.setElement(
            ctx,
            dialog.getView<Button>(R.id.btnOkMessageDialog)
        )
        darkLightTheme.setElement(
            ctx,
            dialog.getView<TextView>(R.id.labelTextMessageDialog)
        )
    }

}