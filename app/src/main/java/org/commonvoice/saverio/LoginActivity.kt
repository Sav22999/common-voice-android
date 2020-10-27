package org.commonvoice.saverio

import android.os.Bundle
import org.commonvoice.saverio.ui.VariableLanguageActivity


class LoginActivity : VariableLanguageActivity(R.layout.activity_login) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.data?.let {
            
        }
    }

}