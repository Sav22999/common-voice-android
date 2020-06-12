package org.commonvoice.saverio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.first_launch.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject


class FirstLaunch : VariableLanguageActivity(R.layout.first_launch) {

    private val firstRunPrefManager: FirstRunPrefManager by inject()

    private var status = 0
    private val RECORD_REQUEST_CODE = 101
    private var languagesListShort =
        arrayOf("en") // don't change manually -> it's imported from strings.xml
    private var languagesList =
        arrayOf("English") // don't change manually -> it's imported from strings.xml
    private var theme: DarkLightTheme = DarkLightTheme()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        seekBarFirstLaunch.isEnabled = false
        seekBarFirstLaunch.progress = 0

        // import languages from array
        languagesList = resources.getStringArray(R.array.languages)
        languagesListShort = resources.getStringArray(R.array.languages_short)

        val textTerms = this.textTermsFirstLaunch
        textTerms.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        textTerms.setOnClickListener {
            openTerms()
        }

        this.buttonNextFirstLaunch.setOnClickListener {
            checkStatus(swipe = false)
        }
        // startup
        checkStatus(start = true)

        buttonSkipFirstLaunch.setOnClickListener {
            checkStatus(next = true)
        }

        buttonOpenTelegramFirstLaunch.setOnClickListener {
            openTelegramGroup()
        }

        // set gestures
        nestedScrollFirstLaunch.setOnTouchListener(object :
            OnSwipeTouchListener(this@FirstLaunch) {
            override fun onSwipeLeft() {
                checkStatus(next = true) //forward
            }

            override fun onSwipeRight() {
                checkStatus(next = false) //back
            }
        })

        // set languages imported
        languageListFirstLaunch.adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languagesList)
        languageListFirstLaunch.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //setLanguage("")
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setLanguage(languagesListShort[position])
                }
            }
        languageListFirstLaunch.setSelection(languagesListShort.indexOf(getString(R.string.language)))

        this.setTheme(this)
    }

    private fun openTerms() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.linkTermsCommonVoice))
            )
        )
    }

    private fun openTelegramGroup() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://bit.ly/3clgfkg")
            )
        )
    }

    private fun checkStatus(start: Boolean = false, next: Boolean = true, swipe: Boolean = true) {
        if (next && status < 7 || !next && status > 0 || start) {
            imageFirstLaunch.setImageResource(R.drawable.robot)
            stopAnimation(imageFirstLaunch)
            buttonSkipFirstLaunch.isGone = true
            stopAnimation(buttonOpenTelegramFirstLaunch)
            buttonOpenTelegramFirstLaunch.isGone = true
            textMessageFirstLaunch.isGone = true
            imageFirstLaunch.imageTintList =
                ContextCompat.getColorStateList(this, R.color.colorGray)
            languageListFirstLaunch.isGone = true
            buttonNextFirstLaunch.text = getString(R.string.btn_tutorial3)
            firstLaunchSectionMiddleBottom.isGone = true
            switchEnableDarkThemeFirstLaunch.isGone = true
            textTermsFirstLaunch.isGone = true

            if (!start) {
                if (next) status++
                else status--
            }
            buttonNextFirstLaunch.setOnClickListener {
                checkStatus(swipe = false)
            }

            seekBarFirstLaunch.progress = status

            val animationFirstLaunch: Int = R.anim.zoom_in_first_launch

            if (status == 0 || start) {
                if (start) {
                    buttonNextFirstLaunch.text = getString(R.string.btn_tutorial1)
                }
                //introduction
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    getString(R.string.txt_introduction_app_first_launch)
                imageFirstLaunch.imageTintList =
                    ContextCompat.getColorStateList(this, R.color.colorTransparent)
                firstLaunchSectionMiddleBottom.isGone = false
                textTermsFirstLaunch.isGone = false
            } else if (status == 1) {
                //microphone
                imageFirstLaunch.setImageResource(R.drawable.ic_microphone)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    getString(R.string.txt_microphone_permission_first_launch)
                if (!checkPermission(Manifest.permission.RECORD_AUDIO)) {
                    buttonNextFirstLaunch.text = getString(R.string.btn_tutorial2)
                    buttonNextFirstLaunch.setOnClickListener {
                        getPermission(Manifest.permission.RECORD_AUDIO)
                    }
                    buttonSkipFirstLaunch.isGone = false
                } else {
                    buttonNextFirstLaunch.setOnClickListener {
                        checkStatus(swipe = false)
                    }
                }
            } else if (status == 2) {
                //storage
                imageFirstLaunch.setImageResource(R.drawable.ic_storage)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    getString(R.string.txt_storage_permission_first_launch)
                if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    buttonNextFirstLaunch.text = getString(R.string.btn_tutorial2)
                    buttonNextFirstLaunch.setOnClickListener {
                        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    buttonSkipFirstLaunch.isGone = false
                } else {
                    buttonNextFirstLaunch.setOnClickListener {
                        checkStatus(swipe = false)
                    }
                }
            } else if (status == 3) {
                //dark theme
                imageFirstLaunch.setImageResource(R.drawable.ic_dark_mode_first_launch)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    getString(R.string.txt_themes_first_launch)
                firstLaunchSectionMiddleBottom.isGone = false
                switchEnableDarkThemeFirstLaunch.isGone = false

                switchEnableDarkThemeFirstLaunch.setOnCheckedChangeListener { _, isChecked ->
                    switchEnableDarkThemeFirstLaunch.isChecked = isChecked
                    setDarkThemeSwitch(isChecked)
                    this.setTheme(this)
                }
                switchEnableDarkThemeFirstLaunch.isChecked = theme.getTheme(this)
            } else if (status == 4) {
                //gestures
                imageFirstLaunch.setImageResource(R.drawable.ic_gestures)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    (getString(R.string.txt_gestures_first_launch) + "\n" + getString(R.string.txt_customise_gestures_first_launch))
            } else if (status == 5) {
                //telegram group
                imageFirstLaunch.setImageResource(R.drawable.ic_telegram)
                imageFirstLaunch.imageTintList =
                    ContextCompat.getColorStateList(this, R.color.colorTransparent)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    getString(R.string.txt_telegram_group_first_launch)
                buttonOpenTelegramFirstLaunch.isGone = false
                startAnimation(buttonOpenTelegramFirstLaunch, animationFirstLaunch)
            } else if (status == 6) {
                //offline mode
                imageFirstLaunch.setImageResource(R.drawable.ic_no_wifi)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    getString(R.string.txt_offline_mode_first_launch)
            } else if (status == 7) {
                //language
                imageFirstLaunch.setImageResource(R.drawable.ic_language)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.text =
                    getString(R.string.txt_choose_language_first_launch)
                languageListFirstLaunch.isGone = false
                buttonNextFirstLaunch.text = getString(R.string.btn_tutorial5)
            }
        } else if (status == 7 && next && !swipe) {
            //close first launch
            finishFirstRun()
        }
    }

    fun setDarkThemeSwitch(status: Boolean) {
        if (status != theme.getTheme(this)) {
            theme.setTheme(this, status)
        }
    }

    fun setTheme(view: Context) {
        val isDark = theme.getTheme(view)
        theme.setElements(view, this.findViewById(R.id.layoutFirstLaunch))
        theme.setElements(view, this.findViewById(R.id.firstLaunchSectionCVAndroid))
        theme.setElements(view, this.findViewById(R.id.firstLaunchSectionDescription))
        theme.setElements(view, this.findViewById(R.id.firstLaunchSectionMiddleBottom))
        theme.setElements(view, this.findViewById(R.id.firstLaunchSectionBottom))

        theme.setElement(isDark, view, 3, findViewById(R.id.firstLaunchSectionCVAndroid))
        theme.setElement(isDark, view, 3, findViewById(R.id.firstLaunchSectionDescription))
        theme.setElement(isDark, view, 3, findViewById(R.id.firstLaunchSectionMiddleBottom))
        theme.setElement(isDark, view, 1, findViewById(R.id.firstLaunchSectionBottom))

        theme.setTextView(!isDark, view, textMessageFirstLaunch, border = false)

        theme.setElement(isDark, view, this.findViewById(R.id.buttonNextFirstLaunch) as Button)
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.buttonOpenTelegramFirstLaunch) as Button
        )
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.seekBarFirstLaunch) as SeekBar,
            R.color.colorBackground,
            R.color.colorBackgroundDT
        )
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                RECORD_REQUEST_CODE
            )
        }
    }

    private fun permissionDenied() {
        // Permission is not granted
        textMessageFirstLaunch.isVisible = true
        if (textMessageFirstLaunch.isGone) {
            startAnimation(textMessageFirstLaunch, R.anim.zoom_in)
        }
        textMessageFirstLaunch.text =
            getString(R.string.txt_permission_denied_first_launch) // permission failed
    }

    private fun permissionObtained() {
        // Permission is granted
        textMessageFirstLaunch.isVisible = true
        if (textMessageFirstLaunch.isGone) {
            startAnimation(textMessageFirstLaunch, R.anim.zoom_in)
        }
        textMessageFirstLaunch.text =
            getString(R.string.txt_permission_obtained_first_launch) // permission successful

        buttonNextFirstLaunch.text = getString(R.string.btn_tutorial3)//next
        buttonSkipFirstLaunch.isGone = true//hide skip button
    }

    private fun finishFirstRun() {
        firstRunPrefManager.main = false
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    fun setLanguage(language: String) {
        mainPrefManager.language = language
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permissionDenied()
                } else {
                    permissionObtained()
                }
            }
        }
    }

}