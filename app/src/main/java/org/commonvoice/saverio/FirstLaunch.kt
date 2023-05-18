package org.commonvoice.saverio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.commonvoice.saverio.databinding.FirstLaunchBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundActivity
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.TranslationHandler
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.koin.android.ext.android.inject


class FirstLaunch : ViewBoundActivity<FirstLaunchBinding>(
    FirstLaunchBinding::inflate
) {

    private val firstRunPrefManager: FirstRunPrefManager by inject()
    private val translationHandler by inject<TranslationHandler>()

    private var status = 0
    private val RECORD_REQUEST_CODE = 101

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.seekBarFirstLaunch.isEnabled = false
        binding.seekBarFirstLaunch.progress = 0

        binding.textTermsFirstLaunch.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.textTermsFirstLaunch.onClick {
            openTerms()
        }

        binding.buttonNextFirstLaunch.onClick {
            checkStatus(swipe = false)
        }
        // startup
        checkStatus(start = true)

        binding.buttonSkipFirstLaunch.onClick {
            checkStatus(next = true)
        }

        binding.buttonOpenTelegramFirstLaunch.onClick {
            openTelegramGroup()
        }

        // set gestures
        binding.nestedScrollFirstLaunch.setOnTouchListener(object :
            OnSwipeTouchListener(this@FirstLaunch) {
            override fun onSwipeLeft() {
                checkStatus(next = true) //forward
            }

            override fun onSwipeRight() {
                checkStatus(next = false) //back
            }
        })

        lifecycleScope.launch {
            translationHandler.updateLanguages()
        }

        setTheme()
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
                Uri.parse("https://t.me/common_voice_android")
            )
        )
    }

    private fun checkStatus(
        start: Boolean = false,
        next: Boolean = true,
        swipe: Boolean = true
    ): Unit = withBinding {

        if (next && status < 7 || !next && status > 0 || start) {
            imageFirstLaunch.setImageResource(R.drawable.robot)
            stopAnimation(imageFirstLaunch)
            buttonSkipFirstLaunch.isGone = true
            stopAnimation(buttonOpenTelegramFirstLaunch)
            buttonOpenTelegramFirstLaunch.isGone = true
            textMessageFirstLaunch.isGone = true
            imageFirstLaunch.imageTintList =
                ContextCompat.getColorStateList(this@FirstLaunch, R.color.colorGray)
            languageListFirstLaunch.isGone = true
            buttonNextFirstLaunch.setText(R.string.btn_tutorial3)
            firstLaunchSectionMiddleBottom.isGone = true
            firstLaunchSectionTheme.isGone = true
            textTermsFirstLaunch.isGone = true

            if (!start) {
                if (next) status++
                else status--
            }
            buttonNextFirstLaunch.onClick {
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
                textDescriptionFirstLaunch.setText(R.string.txt_introduction_app_first_launch)
                imageFirstLaunch.imageTintList =
                    ContextCompat.getColorStateList(this@FirstLaunch, R.color.colorTransparent)
                imageFirstLaunch.setImageResource(R.drawable.robot_half_bust)
                firstLaunchSectionMiddleBottom.isGone = false
                textTermsFirstLaunch.isGone = false
            } else if (status == 1) {
                //microphone
                imageFirstLaunch.setImageResource(R.drawable.ic_microphone)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.setText(R.string.txt_microphone_permission_first_launch)
                if (!checkPermission(Manifest.permission.RECORD_AUDIO)) {
                    buttonNextFirstLaunch.setText(R.string.btn_tutorial2)
                    buttonNextFirstLaunch.onClick {
                        getPermission(Manifest.permission.RECORD_AUDIO)
                    }
                    buttonSkipFirstLaunch.isGone = true
                } else {
                    buttonNextFirstLaunch.onClick {
                        checkStatus(swipe = false)
                    }
                }
            } else if (status == 2) {
                //storage
                imageFirstLaunch.setImageResource(R.drawable.ic_storage)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.setText(R.string.txt_storage_permission_first_launch)
                if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    buttonNextFirstLaunch.setText(R.string.btn_tutorial2)
                    buttonNextFirstLaunch.onClick {
                        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    buttonSkipFirstLaunch.isGone = false
                } else {
                    buttonNextFirstLaunch.onClick {
                        checkStatus(swipe = false)
                    }
                }
            } else if (status == 3) {
                //dark theme
                imageFirstLaunch.setImageResource(R.drawable.ic_dark_mode_first_launch)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.setText(R.string.txt_themes_first_launch)
                firstLaunchSectionMiddleBottom.isGone = false
                firstLaunchSectionTheme.isGone = false

                firstLaunchRadioGroupTheme.check(
                    when (theme.themeType) {
                        "dark" -> R.id.buttonThemeDarkFirstLaunch
                        "auto" -> R.id.buttonThemeAutoFirstLaunch
                        else -> R.id.buttonThemeLightFirstLaunch
                    }
                )

                firstLaunchRadioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
                    theme.themeType = when (checkedId) {
                        R.id.buttonThemeDarkFirstLaunch -> "dark"
                        R.id.buttonThemeAutoFirstLaunch -> "auto"
                        R.id.buttonThemeLightFirstLaunch -> "light"
                        else -> ""
                    }
                    setTheme()
                }
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
                    ContextCompat.getColorStateList(this@FirstLaunch, R.color.colorTransparent)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.setText(R.string.txt_telegram_group_first_launch)
                buttonOpenTelegramFirstLaunch.isGone = false
                startAnimation(buttonOpenTelegramFirstLaunch, animationFirstLaunch)
            } else if (status == 6) {
                //offline mode
                imageFirstLaunch.setImageResource(R.drawable.ic_offline_mode)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.setText(R.string.txt_offline_mode_first_launch)
            } else if (status == 7) {
                //language
                imageFirstLaunch.setImageResource(R.drawable.ic_language)
                startAnimation(imageFirstLaunch, animationFirstLaunch)
                textDescriptionFirstLaunch.setText(R.string.txt_choose_language_first_launch)
                // set languages imported
                val adapter: ArrayAdapter<String> = ArrayAdapter(
                    this@FirstLaunch,
                    R.layout.spinner_text,
                    translationHandler.availableLanguageNames
                )
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_text)
                binding.languageListFirstLaunch.adapter = adapter
                binding.languageListFirstLaunch.onItemSelectedListener =
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
                            mainPrefManager.language =
                                translationHandler.availableLanguageCodes[position]
                        }
                    }
                binding.languageListFirstLaunch.setSelection(
                    translationHandler.availableLanguageCodes.indexOf(
                        getString(R.string.language)
                    )
                )
                languageListFirstLaunch.isGone = false
                buttonNextFirstLaunch.setText(R.string.btn_tutorial5)
            }
        } else if (status == 7 && next && !swipe) {
            //close first launch
            finishFirstRun()
        }
    }

    fun setTheme() = withBinding {
        theme.setElements(this@FirstLaunch, layoutFirstLaunch)
        theme.setElements(this@FirstLaunch, firstLaunchSectionCVAndroid)
        theme.setElements(this@FirstLaunch, firstLaunchSectionDescription)
        theme.setElements(this@FirstLaunch, firstLaunchSectionMiddleBottom)
        theme.setElements(this@FirstLaunch, firstLaunchSectionBottom)

        theme.setElement(this@FirstLaunch, 3, firstLaunchSectionCVAndroid)
        theme.setElement(this@FirstLaunch, 3, firstLaunchSectionDescription)
        theme.setElement(this@FirstLaunch, 3, firstLaunchSectionMiddleBottom)
        theme.setElement(this@FirstLaunch, 1, firstLaunchSectionBottom)
        theme.setElement(this@FirstLaunch, 3, firstLaunchSectionTheme)

        theme.setElement(this@FirstLaunch, buttonThemeLightFirstLaunch)
        theme.setElement(this@FirstLaunch, buttonThemeDarkFirstLaunch)
        theme.setElement(this@FirstLaunch, buttonThemeAutoFirstLaunch)

        theme.setTextView(
            this@FirstLaunch,
            textMessageFirstLaunch,
            border = false,
            darkTeme = !theme.isDark
        )

        theme.setElement(this@FirstLaunch, buttonNextFirstLaunch)
        theme.setElement(
            this@FirstLaunch,
            buttonOpenTelegramFirstLaunch
        )
        theme.setElement(
            this@FirstLaunch,
            seekBarFirstLaunch,
            R.color.colorBackground,
            R.color.colorBackgroundDT
        )

        theme.setElement(
            this@FirstLaunch,
            textCommonVoiceAndroidFirstLaunch,
            background = false,
            textSize = 30F
        )

        theme.setSpinner(
            this@FirstLaunch,
            languageListFirstLaunch,
            R.drawable.spinner_background,
            R.drawable.spinner_background_dark
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
        binding.textMessageFirstLaunch.isVisible = true
        if (binding.textMessageFirstLaunch.isGone) {
            startAnimation(binding.textMessageFirstLaunch, R.anim.zoom_in)
        }
        binding.textMessageFirstLaunch.setText(R.string.txt_permission_denied_first_launch) // permission failed
    }

    private fun permissionObtained() = withBinding {
        // Permission is granted
        textMessageFirstLaunch.isVisible = true
        if (textMessageFirstLaunch.isGone) {
            startAnimation(textMessageFirstLaunch, R.anim.zoom_in)
        }
        textMessageFirstLaunch.setText(R.string.txt_permission_obtained_first_launch) // permission successful

        buttonNextFirstLaunch.setText(R.string.btn_tutorial3)//next
        buttonSkipFirstLaunch.isGone = true//hide skip button
        buttonNextFirstLaunch.onClick {
            checkStatus(swipe = false)
        }
    }

    private fun finishFirstRun() {
        firstRunPrefManager.main = false
        mainPrefManager.hasLanguageChanged = false
        mainPrefManager.hasLanguageChanged2 = false
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
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