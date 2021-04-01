package org.commonvoice.saverio.ui.dialogs.commonTypes

import android.view.LayoutInflater
import androidx.annotation.StringRes
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogInfoBinding
import org.commonvoice.saverio.utils.onClick

class InfoDialog(
    private val message: String? = null,
    @StringRes private val messageRes: Int? = null,
) : CustomDialogInterface<DialogInfoBinding>(
    makeBackgroundTransparent = true,
    preserveLateralMargin = true
) {

    override fun render(inflater: LayoutInflater): DialogInfoBinding {
        return DialogInfoBinding.inflate(inflater).also { binding ->
            message?.let { binding.labelTextMessageDialog.text = it }
            messageRes?.let { binding.labelTextMessageDialog.setText(it) }
            binding.btnOkMessageDialog.onClick { dismiss() }
        }
    }

}