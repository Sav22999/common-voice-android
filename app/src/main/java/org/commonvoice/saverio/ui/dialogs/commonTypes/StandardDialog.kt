package org.commonvoice.saverio.ui.dialogs.commonTypes

import android.view.LayoutInflater
import androidx.annotation.StringRes
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogStandardBinding
import org.commonvoice.saverio.utils.onClick

class StandardDialog(
    private val title: String? = null,
    @StringRes private val titleRes: Int? = null,
    private val message: String? = null,
    @StringRes private val messageRes: Int? = null,
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
            it.btnOkMessageDialog.onClick { dismiss() }
        }
    }

}

