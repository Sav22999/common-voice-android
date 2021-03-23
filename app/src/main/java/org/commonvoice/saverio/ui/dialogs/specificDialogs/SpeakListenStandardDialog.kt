package org.commonvoice.saverio.ui.dialogs.specificDialogs

import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.core.view.isGone
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogStandardBinding
import org.commonvoice.saverio.utils.onClick

class SpeakListenStandardDialog(
    @StringRes private val messageRes: Int,
    private val onDismissListener: () -> Unit,
) : CustomDialogInterface<DialogStandardBinding>(
    makeBackgroundTransparent = true,
    preserveLateralMargin = true
) {

    override fun render(inflater: LayoutInflater): DialogStandardBinding {
        return DialogStandardBinding.inflate(inflater).also {
            messageRes.let { res -> it.labelTextMessageDialog.setText(res) }
            it.btnOkMessageDialog.onClick {
                onDismissListener()
                dismiss()
            }
        }
    }

}

