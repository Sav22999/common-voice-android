package org.commonvoice.saverio

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NoConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noconnection)

        checkConnection()

        var btnCheckNetwork: Button = this.findViewById(R.id.btnCheckAgain)
        btnCheckNetwork.setOnClickListener {
            checkConnection()
        }
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