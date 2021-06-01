package org.commonvoice.saverio.ui.dialogs.messageDialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.core.view.isGone
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogInfoBinding
import org.commonvoice.saverio.databinding.DialogStandardBinding
import org.commonvoice.saverio.databinding.DialogWarningBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.models.Message

class MessageStandardDialog(
    private val context: Context,
    private val message: Message,
) : CustomDialogInterface<DialogStandardBinding>(
    makeBackgroundTransparent = true
) {

    override fun render(inflater: LayoutInflater): DialogStandardBinding {
        return DialogStandardBinding.inflate(inflater).also { binding ->
            binding.labelTitleMessageDialog.isGone = true
            binding.labelTextMessageDialog.text = message.text
            message.button1Text?.let { text ->
                binding.btnOkMessageDialog.text = text
            }
            message.button1Link.let { link ->
                binding.btnOkMessageDialog.onClick {
                    if (link != null) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                    dismiss()
                }
            }
        }
    }

}