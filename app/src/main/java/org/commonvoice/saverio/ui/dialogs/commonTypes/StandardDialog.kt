package org.commonvoice.saverio.ui.dialogs.commonTypes

import android.graphics.Typeface
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogStandardBinding
import org.commonvoice.saverio.utils.onClick

class StandardDialog(
    private val title: String? = null,
    @StringRes private val titleRes: Int? = null,
    private val message: String? = null,
    @StringRes private val messageRes: Int? = null,
    private val buttonText: String? = null,
    @StringRes private val buttonTextRes: Int? = null,
    private val onButtonClick: (() -> Unit)? = null,
    private val button2Text: String? = null,
    @StringRes private val button2TextRes: Int? = null,
    private val onButton2Click: (() -> Unit)? = null,
    private val overrideItalicStyle: Boolean = false,
) : CustomDialogInterface<DialogStandardBinding>(
    makeBackgroundTransparent = true,
    preserveLateralMargin = true
) {

    override fun render(inflater: LayoutInflater): DialogStandardBinding {
        return DialogStandardBinding.inflate(inflater).also {
            title?.let { str -> it.labelTitleMessageDialog.text = str }
            titleRes?.let { res -> it.labelTitleMessageDialog.setText(res) }
            message?.let { str -> it.labelTextMessageDialog.text = str }
            messageRes?.let { res -> it.labelTextMessageDialog.setText(res) }

            if (title == null && titleRes == null) {
                it.labelTitleMessageDialog.isGone = true
            }

            if (button2Text != null || button2TextRes != null) {
                it.btnOk2MessageDialog.isVisible = true
            }
            if (buttonText != null || buttonTextRes != null) {
                val defaultPadding = it.btnOk2MessageDialog.paddingLeft
                it.btnOkMessageDialog.updatePadding(left = defaultPadding, right = defaultPadding)
            }

            buttonText?.let { str -> it.btnOkMessageDialog.text = str }
            buttonTextRes?.let { res -> it.btnOkMessageDialog.setText(res) }
            button2Text?.let { str -> it.btnOk2MessageDialog.text = str }
            button2TextRes?.let { res -> it.btnOk2MessageDialog.setText(res) }

            it.btnOkMessageDialog.onClick {
                onButtonClick?.invoke()
                dismiss()
            }

            it.btnOk2MessageDialog.onClick {
                onButton2Click?.invoke()
                dismiss()
            }

            if (overrideItalicStyle) {
                it.labelTextMessageDialog.setTypeface(null, Typeface.NORMAL)
                it.labelTitleMessageDialog.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

}

