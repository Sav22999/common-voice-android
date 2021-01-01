package org.commonvoice.saverio

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import org.commonvoice.saverio.ui.VariableLanguageActivity


class LoginActivity : VariableLanguageActivity(R.layout.activity_login) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.data?.let {
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.loginFragment, bundleOf(
                    "loginUrl" to "$it"
                )
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (findNavController(R.id.nav_host_fragment).currentDestination?.id == R.id.badgesFragment) {
            super.onBackPressed()
        } else {
            finish()
        }
    }

}