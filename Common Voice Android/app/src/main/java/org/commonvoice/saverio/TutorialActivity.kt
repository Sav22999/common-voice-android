package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_tutorial.*


class TutorialActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var status = 0
    private val RECORD_REQUEST_CODE = 101
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "FIRST_RUN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        this.seekBar.isEnabled=false

        this.seekBar.progress = 0

        this.btn_next.setOnClickListener{
            tutorialStart()
        }

        /*webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        webView.loadUrl("https://voice.mozilla.org/it/")*/
    }

    fun tutorialStart() {
        if (this.status == 0) {
            // start
            tutorialStart1()
        } else if (this.status == 1) {
            // permit
            tutorialStart2()
        } else if (this.status == 2) {
            tutorialStart3()
        } else if (this.status == 3) {
            // close tutorial and open main
            val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
            val editor = sharedPref.edit()
            editor.putBoolean(PREF_NAME, false)
            editor.apply()
            val intent = Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }

    fun tutorialStart1() {
        this.textViewMessage.isVisible = false
        this.textViewMessage.text = ""
        this.seekBar.progress = 1
        this.textView_tutorial.text = getString(R.string.tutorial_text2)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            this.btn_next.text = getString(R.string.btn_tutorial2) // permit
        } else {
            this.btn_next.text = getString(R.string.btn_tutorial3) // next
        }

        this.status = 1
    }

    fun tutorialStart2() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
        }
        else {
            tutorialStart3()
            this.status = 2
        }
    }

    fun tutorialStartPermissionDenied()
    {
        // Permission is not granted
        this.textViewMessage.isVisible = true
        this.textViewMessage.text = "Error: permission fail"
        //Toast.makeText(this, "Error: permission fail", Toast.LENGTH_LONG).show()
        this.btn_next.text = getString(R.string.btn_tutorial4) // try again
    }

    fun tutorialStartPermissionSuccessful()
    {
        // Permission is granted
        this.textViewMessage.isVisible = true
        this.textViewMessage.text = "Permission successful"
        //Toast.makeText(this,"Permission successful",Toast.LENGTH_SHORT).show()
        this.btn_next.text = getString(R.string.btn_tutorial3) // next
        this.status = 2
    }

    fun tutorialStart3() {
        // finish
        this.textViewMessage.isVisible = false
        this.textViewMessage.text = ""
        this.seekBar.progress = 2
        this.textView_tutorial.text = getString(R.string.tutorial_text3)
        this.btn_next.text = getString(R.string.btn_tutorial5)
        this.status = 3
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    tutorialStartPermissionDenied()
                } else {
                    tutorialStartPermissionSuccessful()
                }
            }
        }
    }
}