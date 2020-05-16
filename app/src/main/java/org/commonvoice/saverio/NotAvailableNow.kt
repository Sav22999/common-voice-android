package org.commonvoice.saverio

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.commonvoice.saverio.ui.VariableLanguageActivity

class NotAvailableNow : VariableLanguageActivity(R.layout.not_available_now) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(this)
    }

    fun setTheme(view: Context) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        theme.setElement(isDark, this.findViewById(R.id.layoutNotAvailableNow) as ConstraintLayout)
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.txtNotAvailableNow) as TextView,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
    }

}