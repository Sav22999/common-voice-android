package org.commonvoice.saverio

import android.content.Context
import android.os.Bundle
import org.commonvoice.saverio.databinding.ActivityNoconnectionBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.koin.android.ext.android.inject

class NoConnectionActivity : ViewBoundActivity<ActivityNoconnectionBinding>(
    ActivityNoconnectionBinding::inflate
) {

    private val connectionManager by inject<ConnectionManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkConnection()

        binding.btnCheckAgain.setOnClickListener {
            checkConnection()
        }

        setTheme(this)
    }

    private fun setTheme(view: Context) {
        val theme = DarkLightTheme()

        val isDark = theme.getTheme(view)
        theme.setElement(isDark, binding.layoutNoConnection)
        theme.setElement(isDark, view, binding.btnCheckAgain)
        theme.setElement(
            isDark,
            view,
            binding.txtNoInternetConnection,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
    }

    override fun onBackPressed() {
        checkConnection()
    }

    private fun checkConnection() {
        if (connectionManager.isInternetAvailable) {
            finish()
        }
    }

}