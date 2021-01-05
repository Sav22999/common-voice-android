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

        setTheme()
    }

    fun setTheme() {
        theme.setElement(binding.layoutNotAvailableNow)
        theme.setElement(
            this,
            binding.txtNotAvailableNow,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
    }

}