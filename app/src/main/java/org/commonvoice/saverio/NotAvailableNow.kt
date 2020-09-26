package org.commonvoice.saverio

import android.content.Context
import android.os.Bundle
import org.commonvoice.saverio.databinding.NotAvailableNowBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity

class NotAvailableNow : ViewBoundActivity<NotAvailableNowBinding>(
    NotAvailableNowBinding::inflate
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(this)
    }

    fun setTheme(view: Context) {
        val theme: DarkLightTheme = DarkLightTheme()

        val isDark = theme.getTheme(view)
        theme.setElement(isDark, binding.layoutNotAvailableNow)
        theme.setElement(
            isDark,
            view,
            binding.txtNotAvailableNow,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
    }

}