package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.first_run.*
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject


class FirstLaunch : AppCompatActivity() {

    private val mainPrefManager: MainPrefManager by inject()

    private lateinit var webView: WebView
    private var status = 0
    private val RECORD_REQUEST_CODE = 101
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "FIRST_RUN"
    var languages_list_short =
        arrayOf("en") // don't change manually -> it's imported from strings.xml
    var languages_list =
        arrayOf("English") // don't change manually -> it's imported from strings.xml

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_run)

        this.seekBarFirstLaunch.isEnabled = false

        this.seekBarFirstLaunch.progress = 0

        // import languages from array
        this.languages_list = resources.getStringArray(R.array.languages)
        this.languages_list_short = resources.getStringArray(R.array.languages_short)

        var txtTerms = this.textTermsFirstLaunch
        txtTerms.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txtTerms.setOnClickListener {
            openTerms()
        }

        this.buttonNextFirstLaunch.setOnClickListener {
            tutorialStart()
        }
        tutorialStart()

        var txtSkip = this.buttonSkipFirstLaunch
        //txtSkip.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txtSkip.setOnClickListener {
            skipPermission()
        }

        nestedScrollTutorial.setOnTouchListener(object :
            OnSwipeTouchListener(this@FirstLaunch) {
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
            /*
            override fun onSwipeTop() {
                super.onSwipeTop()
                if (status == 0 || status == 1) {
                    openTerms()
                }
            }
            */
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
        this.textMessageFirstLaunch.text = getString(R.string.txt_introduction_app_first_launch)
        this.seekBarFirstLaunch.progress = 0
        this.tutorialSectionTerms.isGone = false
        this.textDescriptionFirstLaunch.text = getString(R.string.txt_introduction_app_first_launch)
        var txtSkip = this.buttonSkipFirstLaunch
        txtSkip.isGone = true
        this.textMessageFirstLaunch.isVisible = false
        this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial1) // next
        this.status = 1
    }

    fun microphonePermission() {
        this.textMessageFirstLaunch.isVisible = false
        this.textMessageFirstLaunch.text = ""
        this.seekBarFirstLaunch.progress = 1
        this.tutorialSectionTerms.isGone = true
        this.textDescriptionFirstLaunch.text = getString(R.string.tutorial_text2)
        var txtSkip = this.buttonSkipFirstLaunch
        txtSkip.isGone = false
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial2) // permit
        } else {
            this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial3) // next
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
            var txtSkip = this.buttonSkipFirstLaunch
            txtSkip.isGone = true
            storagePermission()
        }
    }

    fun tutorialStartPermissionDenied() {
        // Permission is not granted
        this.textMessageFirstLaunch.isVisible = true
        this.textMessageFirstLaunch.text =
            getString(R.string.txt_permission_failed) // permission failed
        //Toast.makeText(this, "Error: permission fail", Toast.LENGTH_LONG).show()
        this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial4) // try again
    }

    fun tutorialStartPermissionSuccessful() {
        // Permission is granted
        this.textMessageFirstLaunch.isVisible = true
        this.textMessageFirstLaunch.text =
            getString(R.string.txt_permission_successful) // permission successful
        //Toast.makeText(this,"Permission successful",Toast.LENGTH_SHORT).show()
        this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial3) // next
        if (this.status == 2) {
            //microphone permission
            this.status = 3
        } else if (this.status == 4) {
            //storage permission
            this.status = 5
        }
    }

    fun storagePermission() {
        this.textMessageFirstLaunch.isVisible = false
        this.textMessageFirstLaunch.text = ""
        this.textDescriptionFirstLaunch.text = getString(R.string.tutorial_text3)
        this.seekBarFirstLaunch.progress = 2
        var txtSkip = this.buttonSkipFirstLaunch
        txtSkip.isGone = false
        this.languageListTutorial.isGone = true
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial2) // permit
        } else {
            this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial3) // next
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
            var txtSkip = this.buttonSkipFirstLaunch
            txtSkip.isGone = true
            tutorialStart4()
        }
    }

    fun tutorialStart4() {
        // finish
        this.textMessageFirstLaunch.isVisible = false
        this.textMessageFirstLaunch.text = ""
        this.seekBarFirstLaunch.progress = 3
        this.textDescriptionFirstLaunch.text = getString(R.string.tutorial_text4)
        this.buttonNextFirstLaunch.text = getString(R.string.btn_tutorial5)
        this.languageListTutorial.isGone = false
        var txtSkip = this.buttonSkipFirstLaunch
        txtSkip.isGone = true

        var languages = findViewById<Spinner>(R.id.languageListTutorial)
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
        mainPrefManager.language = language

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