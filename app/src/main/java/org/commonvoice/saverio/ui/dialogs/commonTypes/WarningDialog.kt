package org.commonvoice.saverio.ui.dialogs.commonTypes

import android.view.LayoutInflater
import androidx.annotation.StringRes
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogWarningBinding
import org.commonvoice.saverio.utils.onClick

class WarningDialog(
    private val message: String? = null,
    @StringRes private val messageRes: Int? = null,
) : CustomDialogInterface<DialogWarningBinding>(
    makeBackgroundTransparent = true,
    preserveLateralMargin = true
) {

    override fun render(inflater: LayoutInflater): DialogWarningBinding {
        return DialogWarningBinding.inflate(inflater).also { binding ->
            message?.let { binding.labelTextMessageDialog.text = it }
            messageRes?.let { binding.labelTextMessageDialog.setText(it) }
            binding.btnOkMessageDialog.onClick { dismiss() }
        }
    }

}