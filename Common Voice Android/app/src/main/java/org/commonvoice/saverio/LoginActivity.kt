package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkRecordVoicePermission()

        var btnLoginSignUp: Button = findViewById(R.id.btn_login_signup)
        btnLoginSignUp.setOnClickListener {
            openWebBrowser()
        }
        var txtEmail: EditText = findViewById(R.id.txt_email_login)
        txtEmail.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                openWebBrowser()
                return@OnKeyListener true
            }
            false
        })
    }

    fun openWebBrowser() {
        val email = findViewById<EditText>(R.id.txt_email_login).text

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setContentView(R.layout.fragment_webbrowser)

            webView = findViewById(R.id.webViewBrowser)

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.webViewClient = WebViewClient()
            webView.loadUrl("https://accounts.firefox.com/signup?email=" + email)
            webView.loadUrl("https://voice.mozilla.org/api/v1/it/clips")
        }
    }

    fun checkRecordVoicePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET),
                RECORD_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    checkRecordVoicePermission()
                }
            }
        }
    }
}