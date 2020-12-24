package org.commonvoice.saverio

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.ui.login.LoginFragment


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
        return if (findNavController(R.id.nav_host_fragment).currentDestination?.id != R.id.profileFragment) {
            findNavController(R.id.nav_host_fragment).navigateUp()
        } else {
            super.onSupportNavigateUp()
        }
    }

    fun closeAndReopenLogin() {
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    fun goToProfile() {
        findNavController(R.id.profileFragment).navigateUp()
    }
}