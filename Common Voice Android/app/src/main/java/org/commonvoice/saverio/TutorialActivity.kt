package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_tutorial.*


class TutorialActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var status = 0
    private val RECORD_REQUEST_CODE = 101
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "FIRST_RUN"
    private val LANGUAGE_NAME = "LANGUAGE"
    var languages_list_short =
        arrayOf("en") // don't change manually -> it's imported from strings.xml
    var languages_list =
        arrayOf("English") // don't change manually -> it's imported from strings.xml

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        this.seekBar.isEnabled = false

        this.seekBar.progress = 0

        // import languages from array
        this.languages_list = resources.getStringArray(R.array.languages)
        this.languages_list_short = resources.getStringArray(R.array.languages_short)

        var txt_terms = this.textView_tutorialTerms
        txt_terms.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txt_terms.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.linkTermsCommonVoice)))
            startActivity(browserIntent)
        }

        this.btn_next.setOnClickListener {
            tutorialStart()
        }
    }

    fun tutorialStart() {
        if (this.status == 0) {
            // start
            tutorialStart1()
        } else if (this.status == 1) {
            // ask microphone permission
            tutorialStart2()
        } else if (this.status == 2) {
            askStoragePermission()
        } else if (this.status == 3) {
            // ask storage permission
            tutorialStart3()
        } else if (this.status == 4) {
            tutorialStart4()
        } else if (this.status == 5) {
            // close tutorial and open main
            val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
            var editor = sharedPref.edit()
            editor.putBoolean(PREF_NAME, false)
            editor.apply()

            val intent = Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }

    fun tutorialStart1() {
        this.textTutorialMessage.isVisible = false
        this.textTutorialMessage.text = ""
        this.seekBar.progress = 1
        this.textView_tutorialTerms.isGone = true
        this.textView_tutorial.text = getString(R.string.tutorial_text2)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.btn_next.text = getString(R.string.btn_tutorial2) // permit
        } else {
            this.btn_next.text = getString(R.string.btn_tutorial3) // next
        }
        this.status = 1
        //println(" -->> tutorialStart1")
    }

    fun tutorialStart2() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_REQUEST_CODE
            )
        } else {
            askStoragePermission()
        }
        //println(" -->> tutorialStart2")
    }

    fun tutorialStartPermissionDenied() {
        // Permission is not granted
        this.textTutorialMessage.isVisible = true
        this.textTutorialMessage.text = "Error: permission fail"
        //Toast.makeText(this, "Error: permission fail", Toast.LENGTH_LONG).show()
        this.btn_next.text = getString(R.string.btn_tutorial4) // try again
    }

    fun tutorialStartPermissionSuccessful() {
        // Permission is granted
        this.textTutorialMessage.isVisible = true
        this.textTutorialMessage.text = "Permission successful"
        //Toast.makeText(this,"Permission successful",Toast.LENGTH_SHORT).show()
        this.btn_next.text = getString(R.string.btn_tutorial3) // next
        if (this.status == 1) {
            //microphone permission
            this.status = 2
        } else if (this.status == 3) {
            //storage permission
            this.status = 4
        }
    }

    fun askStoragePermission() {
        this.textTutorialMessage.isVisible = false
        this.textTutorialMessage.text = ""
        this.textView_tutorial.text = getString(R.string.tutorial_text3)
        this.seekBar.progress = 2
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.btn_next.text = getString(R.string.btn_tutorial2) // permit
        } else {
            this.btn_next.text = getString(R.string.btn_tutorial3) // next
        }
        this.status = 3
        //println(" -->> askStoragePermission")
    }

    fun tutorialStart3() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE
            )
        } else {
            tutorialStart4()
        }
        //println(" -->> tutorialStart3")
    }

    fun tutorialStart4() {
        // finish
        this.textTutorialMessage.isVisible = false
        this.textTutorialMessage.text = ""
        this.seekBar.progress = 3
        this.textView_tutorial.text = getString(R.string.tutorial_text4)
        this.btn_next.text = getString(R.string.btn_tutorial5)
        this.languageListTutorial.isVisible = true

        var languages = findViewById(R.id.languageListTutorial) as Spinner
        languages.adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languages_list)
        languages.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //setLanguage("")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setLanguage(languages_list_short.get(position))
                status = 5
            }
        }
        languages.setSelection(languages_list_short.indexOf(getString(R.string.language)))
    }

    fun setLanguage(language: String) {
        val sharedPref: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putString(LANGUAGE_NAME, language)
        editor.apply()

        //Toast.makeText(this,"Language: "+language+" index: "+languages_list_short.indexOf(language), Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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