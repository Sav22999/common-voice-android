package org.commonvoice.saverio

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.ui.login.LoginFragment
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.koin.android.ext.android.inject


class LoginActivity : VariableLanguageActivity(R.layout.activity_login) {

    private val statsPrefManager by inject<StatsPrefManager>()

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

    fun closeAndReopenLogin() {
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    fun goToProfile() {
        findNavController(R.id.profileFragment).navigateUp()
    }

    fun close() {
        finish()
    }

    fun resetDataLoginLogout() {
        statsPrefManager.dailyGoalObjective = 0
        statsPrefManager.todayValidated = 0
        statsPrefManager.todayRecorded = 0
        statsPrefManager.allTimeLevel = 0
        statsPrefManager.allTimeRecorded = 0
        statsPrefManager.allTimeValidated = 0
    }
}