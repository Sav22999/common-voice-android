package org.commonvoice.saverio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone

class WebBrowser : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webbrowser)

        if (checkConnection()) {
            navigateWebBrowser()
        } else {
            finish()
        }
    }

    fun checkConnection(): Boolean {
        if (LoginActivity.checkInternet(this))
        {
            return true
        } else {
            openNoConnection()
            return false
        }
    }

    companion object {
        fun checkInternet(context: Context):Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                //Connection OK
                return true
            }
            else {
                //No connection
                return false
            }

        }
    }

    fun openNoConnection() {
        val intent = Intent(this, NoConnectionActivity::class.java).also {
            startActivity(it)
        }
    }

    fun startAnimation(img: ImageView) {
        var animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.login)
        img.startAnimation(animation)
    }

    fun stopAnimation(img: ImageView) {
        img.clearAnimation()
    }

    fun navigateWebBrowser() {
        var txtLoading: TextView = findViewById(R.id.txtLoadingWebBrowser)
        var bgLoading: ImageView = findViewById(R.id.imgBackgroundWebBrowser)
        var imgLoading: ImageView = findViewById(R.id.imgRobotWebBrowser)

        webView = findViewById(R.id.webViewBrowser)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                // Loading started
                txtLoading.isGone = false
                bgLoading.isGone = false
                imgLoading.isGone = false
                startAnimation(imgLoading)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // Loading finished
                txtLoading.isGone = true
                bgLoading.isGone = true
                imgLoading.isGone = true
                stopAnimation(imgLoading)
            }
        }
        webView.loadUrl("https://voice.allizom.org/it")
    }
}