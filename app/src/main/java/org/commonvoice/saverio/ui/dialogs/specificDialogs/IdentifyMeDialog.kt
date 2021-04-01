package org.commonvoice.saverio.ui.dialogs.specificDialogs

import android.view.LayoutInflater
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogIdentifymeBinding
import org.commonvoice.saverio.utils.onClick

class IdentifyMeDialog(
    private val userIdString: String,
    private val onCopyClick: () -> Unit,
) : CustomDialogInterface<DialogIdentifymeBinding>(
    makeBackgroundTransparent = true
) {

    override fun render(inflater: LayoutInflater): DialogIdentifymeBinding {
        return DialogIdentifymeBinding.inflate(inflater).also { binding ->
            binding.labelTextMessageDialog.text = userIdString
            binding.btnOkMessageDialog.onClick { dismiss() }
            binding.imageCopyText.onClick(onCopyClick)
        }
    }

}