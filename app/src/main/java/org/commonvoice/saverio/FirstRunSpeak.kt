package org.commonvoice.saverio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.activity_speak.*
import kotlinx.android.synthetic.main.first_run_speak.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.koin.android.ext.android.inject

class FirstRunSpeak : VariableLanguageActivity(R.layout.first_run_speak) {

    private val firstRunPrefManager: FirstRunPrefManager by inject()

    var status: Int = 0
    private var PRIVATE_MODE = 0
    private val FIRST_RUN_SPEAK = "FIRST_RUN_SPEAK"
    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.seekBarFirstRunSpeak.isEnabled = false
        this.seekBarFirstRunSpeak.progress = 0

        goNextOrBack()
        var btnNext: Button = this.findViewById(R.id.btnNextSpeak)
        btnNext.setOnClickListener {
            goNextOrBack()
        }

        layoutFirstRunSpeak.setOnTouchListener(object :
            OnSwipeTouchListener(this@FirstRunSpeak) {
            override fun onSwipeLeft() {
                goNextOrBack(true)
            }

            override fun onSwipeRight() {
                goNextOrBack(false)
            }
        })

        setTheme(this)
    }

    fun setTheme(view: Context) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        theme.setElements(view, this.findViewById(R.id.firstRunSpeakSectionBottom))
        theme.setElement(isDark, view, 1, findViewById(R.id.firstRunSpeakSectionBottom))
        theme.setElement(isDark, this.findViewById(R.id.layoutFirstRunSpeak) as ConstraintLayout)
        theme.setElement(isDark, view, this.findViewById(R.id.btnNextSpeak) as Button)
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.seekBarFirstRunSpeak) as SeekBar,
            R.color.colorBackground,
            R.color.colorBackgroundDT
        )
    }

    override fun onBackPressed() {
        finish()
    }

    fun goNextOrBack(next: Boolean = true) {
        var btnNext: Button = this.findViewById(R.id.btnNextSpeak)
        var txtNumberBottom: Button = this.findViewById(R.id.btnNumberBottomSpeak)
        var txtTextBottom: TextView = this.findViewById(R.id.txtTutorialMessageBottomSpeak)
        var txtNumberTop: Button = this.findViewById(R.id.btnNumberTopSpeak)
        var txtTextTop: TextView = this.findViewById(R.id.txtTutorialMessageTopSpeak)
        var txtOne: Button = this.findViewById(R.id.btnOneSpeak)
        var txtTwo: Button = this.findViewById(R.id.btnTwoSpeak)
        var txtThree: Button = this.findViewById(R.id.btnThreeSpeak)
        var txtFour: Button = this.findViewById(R.id.btnFourSpeak)
        var txtEight: Button = this.findViewById(R.id.btnEightSpeak)
        var txtNine: Button = this.findViewById(R.id.btnNineSpeak)
        var btnRecord: ImageView = this.findViewById(R.id.imgBtnRecordSpeak)
        var btnListenAgain: ImageView = this.findViewById(R.id.imgBtnListenAgainSpeak)
        var btnSend: ImageView = this.findViewById(R.id.imgBtnSendSpeak)
        var txtSend: ImageView = this.findViewById(R.id.imgTxt4Speak)
        if (this.status >= 0 && this.status < 9) {
            txtNumberBottom.isGone = true
            txtTextBottom.isGone = true
            txtNumberTop.isGone = true
            txtTextTop.isGone = true
            txtOne.isGone = true
            txtTwo.isGone = true
            txtFour.isGone = true
            txtThree.isGone = true
            txtEight.isGone = true
            txtNine.isGone = true
            btnRecord.setImageResource(R.drawable.speak_cv)
            btnListenAgain.isGone = true
            btnSend.isGone = true
            txtSend.isGone = true
        }

        if (next) this.seekBarFirstRunSpeak.progress = this.status
        else if (!next && this.status > 1) this.seekBarFirstRunSpeak.progress = this.status - 2

        if (this.status == 0 || this.status == 2 && !next || this.status == 1 && !next) {
            this.status = 1
            btnNext.setText(getString(R.string.btn_tutorial1))
            txtNumberBottom.setText("1")
            txtTextBottom.setText(getString(R.string.txt1_tutorial_speak))
            txtNumberBottom.isGone = false
            txtTextBottom.isGone = false
            txtOne.isGone = false
            stopAnimation(txtTwo)
            startAnimation(txtOne)
        } else if (this.status == 1 || this.status == 3 && !next) {
            this.status = 2
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("2")
            txtTextTop.setText(getString(R.string.txt2_tutorial_speak_and_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtTwo.isGone = false
            stopAnimation(txtOne)
            stopAnimation(txtThree)
            startAnimation(txtTwo)
        } else if (this.status == 2 || this.status == 4 && !next) {
            this.status = 3
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("3")
            txtTextTop.setText(getString(R.string.txt3_tutorial_speak))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtThree.isGone = false
            stopAnimation(txtTwo)
            stopAnimation(txtFour)
            startAnimation(txtThree)
        } else if (this.status == 3 || this.status == 5 && !next) {
            this.status = 4
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("4")
            txtTextTop.setText(getString(R.string.txt4_tutorial_speak))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("4")
            stopAnimation(txtThree)
            stopAnimation(txtFour)
            startAnimation(txtFour)
        } else if (this.status == 4 || this.status == 6 && !next) {
            this.status = 5
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("5")
            txtTextTop.setText(getString(R.string.txt5_tutorial_speak))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("5")
            btnRecord.setImageResource(R.drawable.stop_cv)
            stopAnimation(txtFour)
            startAnimation(txtFour)
        } else if (this.status == 5 || this.status == 7 && !next) {
            this.status = 6
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("6")
            txtTextTop.setText(getString(R.string.txt6_tutorial_speak))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("6")
            btnRecord.setImageResource(R.drawable.listen2_cv)
            stopAnimation(txtFour)
            startAnimation(txtFour)
        } else if (this.status == 6 || this.status == 8 && !next) {
            this.status = 7
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("7")
            txtTextTop.setText(getString(R.string.txt7_tutorial_speak))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("7")
            btnListenAgain.isGone = false
            btnSend.isGone = false
            txtSend.isGone = false
            btnRecord.setImageResource(R.drawable.speak2_cv)
            stopAnimation(txtFour)
            stopAnimation(txtEight)
            startAnimation(txtFour)
        } else if (this.status == 7 || this.status == 9 && !next) {
            this.status = 8
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberBottom.setText("8")
            txtTextBottom.setText(getString(R.string.txt8_tutorial_speak))
            txtNumberTop.isGone = true
            txtTextTop.isGone = true
            txtNumberBottom.isGone = false
            txtTextBottom.isGone = false
            txtEight.isGone = false
            txtNine.isGone = true
            btnListenAgain.isGone = false
            btnSend.isGone = false
            txtSend.isGone = false
            btnRecord.setImageResource(R.drawable.speak2_cv)
            stopAnimation(txtFour)
            stopAnimation(txtNine)
            startAnimation(txtEight)
        } else if (this.status == 8 || this.status == 10 && !next) {
            this.status = 9
            btnNext.setText(getString(R.string.btn_tutorial5))
            txtNumberTop.setText("9")
            txtTextTop.setText(getString(R.string.txt9_tutorial_speak))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtNumberBottom.isGone = true
            txtTextBottom.isGone = true
            txtNine.isGone = false
            btnListenAgain.isGone = false
            btnRecord.setImageResource(R.drawable.speak2_cv)
            btnSend.isGone = false
            txtSend.isGone = false
            stopAnimation(txtEight)
            startAnimation(txtNine)
        } else if (this.status == 9) {
            firstRunPrefManager.speak = false
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_REQUEST_CODE
                )
            } else {
                openActualSpeakSection()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openActualSpeakSection()
                }
            }
        }
    }

    private fun openActualSpeakSection() {
        Intent(this, SpeakActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    private fun startAnimation(img: Button) {
        if (mainPrefManager.areAnimationsEnabled) {
            var animation: Animation =
                AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_in)
            img.startAnimation(animation)
        }
    }

    private fun stopAnimation(img: Button) {
        if (mainPrefManager.areAnimationsEnabled) {
            img.clearAnimation()
        }
    }

}