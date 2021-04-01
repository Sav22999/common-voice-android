package org.commonvoice.saverio.ui.dialogs.specificDialogs

import android.view.LayoutInflater
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.databinding.DialogOfflineModeBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager

class OfflineModeDialog(
    private val mainPrefManager: MainPrefManager,
) : CustomDialogInterface<DialogOfflineModeBinding>(
    makeBackgroundTransparent = true
) {

    override fun render(inflater: LayoutInflater): DialogOfflineModeBinding {
        return DialogOfflineModeBinding.inflate(inflater).also { binding ->
            binding.btnOkMessageDialogOfflineMode.onClick {
                mainPrefManager.showOfflineModeMessage = binding.checkDoNotShowAnymoreOfflineMode.isChecked
                dismiss()
            }
        }
    }

}