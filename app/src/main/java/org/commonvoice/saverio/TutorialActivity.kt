package org.commonvoice.saverio

import OnSwipeTouchListener
import android.Manifest
import android.content.Context
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
import androidx.constraintlayout.widget.ConstraintLayout
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

        var txtTerms = this.textView_tutorialTerms
        txtTerms.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txtTerms.setOnClickListener {
            openTerms()
        }

        this.btn_next.setOnClickListener {
            tutorialStart()
        }
        tutorialStart()

        var txtSkip = this.textSkipTutorial
        txtSkip.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txtSkip.setOnClickListener {
            skipPermission()
        }

        nestedScrollTutorial.setOnTouchListener(object :
            OnSwipeTouchListener(this@TutorialActivity) {
            override fun onSwipeLeft() {
                if (status < 6) {
                    if (status == 0 || status == 1) {
                        microphonePermission()
                        status = 2
                    } else if (status == 2 || status == 3) {
                        storagePermission()
                        status = 4
                    } else if (status == 4 || status == 5) {
                        tutorialStart4()
                    }
                }
            }

            override fun onSwipeRight() {
                if (status > 0) {
                    if (status == 1 || status == 2 || status == 3) {
                        tutorialStart0()
                        status = 1
                    } else if (status == 4 || status == 5) {
                        microphonePermission()
                        status = 2
                    } else if (status == 6) {
                        storagePermission()
                        status = 4
                    }
                }
            }

            override fun onSwipeTop() {
                super.onSwipeTop()
                if (status == 0 || status == 1) {
                    openTerms()
                }
            }
        })
    }

    fun openTerms() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.linkTermsCommonVoice))
            )
        )
    }

    fun tutorialStart() {
        if (this.status == 0) {
            // first screen
            tutorialStart0()
        } else if (this.status == 1) {
            microphonePermission()
        } else if (this.status == 2) {
            // ask microphone permission
            askMicrophonePermission()
        } else if (this.status == 3) {
            storagePermission()
        } else if (this.status == 4) {
            // ask storage permission
            askStoragePermission()
        } else if (this.status == 5) {
            tutorialStart4()
        } else if (this.status == 6) {
            // close tutorial and open main
            getSharedPreferences(PREF_NAME, PRIVATE_MODE).edit().putBoolean(PREF_NAME, false)
                .apply()

            val intent = Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }

    fun tutorialStart0() {
        this.textTutorialMessage.text = getString(R.string.tutorial_terms)
        this.seekBar.progress = 0
        this.textView_tutorialTerms.isGone = false
        this.textView_tutorial.text = getString(R.string.tutorial_text1)
        var txtSkip = this.textSkipTutorial
        txtSkip.isGone = true
        this.textTutorialMessage.isVisible = false
        this.btn_next.text = getString(R.string.btn_tutorial1) // next
        this.status = 1
    }

    fun microphonePermission() {
        this.textTutorialMessage.isVisible = false
        this.textTutorialMessage.text = ""
        this.seekBar.progress = 1
        this.textView_tutorialTerms.isGone = true
        this.textView_tutorial.text = getString(R.string.tutorial_text2)
        var txtSkip = this.textSkipTutorial
        txtSkip.isGone = false
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.btn_next.text = getString(R.string.btn_tutorial2) // permit
        } else {
            this.btn_next.text = getString(R.string.btn_tutorial3) // next
        }
        this.status = 2
    }

    fun askMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_REQUEST_CODE
            )
        } else {
            var txtSkip = this.textSkipTutorial
            txtSkip.isGone = true
            storagePermission()
        }
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
        if (this.status == 2) {
            //microphone permission
            this.status = 3
        } else if (this.status == 4) {
            //storage permission
            this.status = 5
        }
    }

    fun storagePermission() {
        this.textTutorialMessage.isVisible = false
        this.textTutorialMessage.text = ""
        this.textView_tutorial.text = getString(R.string.tutorial_text3)
        this.seekBar.progress = 2
        var txtSkip = this.textSkipTutorial
        txtSkip.isGone = false
        this.languageListTutorial.isGone = true
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.btn_next.text = getString(R.string.btn_tutorial2) // permit
        } else {
            this.btn_next.text = getString(R.string.btn_tutorial3) // next
        }
        this.status = 4
    }

    fun askStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE
            )
        } else {
            var txtSkip = this.textSkipTutorial
            txtSkip.isGone = true
            tutorialStart4()
        }
    }

    fun tutorialStart4() {
        // finish
        this.textTutorialMessage.isVisible = false
        this.textTutorialMessage.text = ""
        this.seekBar.progress = 3
        this.textView_tutorial.text = getString(R.string.tutorial_text4)
        this.btn_next.text = getString(R.string.btn_tutorial5)
        this.languageListTutorial.isGone = false
        var txtSkip = this.textSkipTutorial
        txtSkip.isGone = true

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
                status = 6
            }
        }
        languages.setSelection(languages_list_short.indexOf(getString(R.string.language)))
    }

    fun setLanguage(language: String) {
        getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE).edit().putString(LANGUAGE_NAME, language)
            .apply()

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

    fun skipPermission() {
        if (this.status == 2) {
            // skip microphone
            this.status = 3
            tutorialStart()
        } else if (this.status == 4) {
            // skip storage
            this.status = 5
            tutorialStart()
        }
    }
}