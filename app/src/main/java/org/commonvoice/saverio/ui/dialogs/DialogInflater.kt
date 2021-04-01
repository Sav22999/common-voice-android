package org.commonvoice.saverio.ui.dialogs

import androidx.viewbinding.ViewBinding
import com.github.mrindeciso.advanced_dialogs.base.AbstractDialogManager
import org.commonvoice.saverio.ui.theming.ThemingEngine

class DialogInflater(
    private val themingEngine: ThemingEngine
): AbstractDialogManager() {

    override val _systematicOperation: (ViewBinding) -> Unit = {
        themingEngine.applyTheme(it)
    }

}