package org.commonvoice.saverio

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.commonvoice.saverio.ui.VariableLanguageActivity

class NoConnectionActivity : VariableLanguageActivity(R.layout.activity_noconnection) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkConnection()

        var btnCheckNetwork: Button = this.findViewById(R.id.btnCheckAgain)
        btnCheckNetwork.setOnClickListener {
            checkConnection()
        }

        setTheme(this)
    }

    fun setTheme(view: Context) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        theme.setElement(isDark, this.findViewById(R.id.layoutNoConnection) as ConstraintLayout)
        theme.setElement(isDark, view, this.findViewById(R.id.btnCheckAgain) as Button)
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.txtNoInternetConnection) as TextView,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
    }

    override fun onBackPressed() {
        checkConnection()
    }

    fun checkConnection(): Boolean {
        if (MainActivity.checkInternet(this)) {
            finish()
            return true
        } else {
            return false
        }
    }

    companion object {
        fun checkInternet(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                //Connection OK
                return true
            } else {
                //No connection
                return false
            }

        }
    }

}