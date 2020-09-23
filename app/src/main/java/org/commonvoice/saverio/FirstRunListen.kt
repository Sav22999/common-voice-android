package org.commonvoice.saverio

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.first_run_listen.*
import kotlinx.android.synthetic.main.fragment_settings.*
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.koin.android.ext.android.inject


class FirstRunListen : VariableLanguageActivity(R.layout.first_run_listen) {

    private val firstRunPrefManager: FirstRunPrefManager by inject()

    var status: Int = 0
    private var PRIVATE_MODE = 0
    private val FIRST_RUN_LISTEN = "FIRST_RUN_LISTEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.seekBarFirstRunListen.isEnabled = false
        this.seekBarFirstRunListen.progress = 0

        goNextOrBack()
        val btnNext: Button = this.findViewById(R.id.btnNextListen)
        btnNext.setOnClickListener {
            goNextOrBack()
        }

        layoutFirstRunListen.setOnTouchListener(object :
            OnSwipeTouchListener(this@FirstRunListen) {
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
        val theme: DarkLightTheme = DarkLightTheme()

        val isDark = theme.getTheme(view)
        theme.setElements(view, this.findViewById(R.id.firstRunListenSectionBottom))
        theme.setElement(isDark, view, 1, findViewById(R.id.firstRunListenSectionBottom))
        theme.setElement(isDark, this.findViewById(R.id.layoutFirstRunListen) as ConstraintLayout)
        theme.setElements(view, this.findViewById(R.id.layoutFirstRunListenNoSmartphone))
        theme.setElement(
            isDark,
            this.findViewById(R.id.layoutFirstRunListenNoSmartphone) as ConstraintLayout
        )
        theme.setElement(isDark, view, this.findViewById(R.id.btnReadNowGuidelinesListen) as Button)
        theme.setElement(isDark, view, this.findViewById(R.id.btnNextListen) as Button)
        theme.setElement(
            isDark,
            view,
            this.findViewById(R.id.seekBarFirstRunListen) as SeekBar,
            R.color.colorBackground,
            R.color.colorBackgroundDT
        )
    }

    override fun onBackPressed() {
        finish()
    }

    fun goNextOrBack(next: Boolean = true) {
        val btnNext: Button = this.findViewById(R.id.btnNextListen)
        val txtNumberBottom: Button = this.findViewById(R.id.btnNumberBottomListen)
        val txtTextBottom: TextView = this.findViewById(R.id.txtTutorialMessageBottomListen)
        val txtNumberTop: Button = this.findViewById(R.id.btnNumberTopListen)
        val txtTextTop: TextView = this.findViewById(R.id.txtTutorialMessageTopListen)
        val txtOne: Button = this.findViewById(R.id.btnOneListen)
        val txtTwo: Button = this.findViewById(R.id.btnTwoListen)
        val txtThree: Button = this.findViewById(R.id.btnThreeListen)
        val txtFour: Button = this.findViewById(R.id.btnFourListen)
        val txtSeven: Button = this.findViewById(R.id.btnSevenListen)
        val txtEight: Button = this.findViewById(R.id.btnEightListen)
        val btnPlay: ImageView = this.findViewById(R.id.imgBtnPlayListen)
        val btnYes: ImageView = this.findViewById(R.id.imgBtnYesListen)
        val btnNo: ImageView = this.findViewById(R.id.imgBtnNoListen)
        val btnReadGuidelines: Button = this.findViewById(R.id.btnReadNowGuidelinesListen)
        if (this.status >= 0 && this.status < 9) {
            txtNumberBottom.isGone = true
            txtTextBottom.isGone = true
            txtNumberTop.isGone = true
            txtTextTop.isGone = true
            txtOne.isGone = true
            txtTwo.isGone = true
            txtThree.isGone = true
            txtFour.isGone = true
            txtSeven.isGone = true
            txtEight.isGone = true
            btnPlay.setImageResource(R.drawable.listen_cv)
            btnYes.isGone = true
            btnNo.isGone = true
        }

        if (next) this.seekBarFirstRunListen.progress = this.status
        else if (!next && this.status > 1) this.seekBarFirstRunListen.progress = this.status - 2

        if (this.status == 0 && next || this.status == 2 && !next || this.status == 1 && !next) {
            this.status = 1
            btnNext.setText(getString(R.string.btn_tutorial1))
            txtNumberBottom.setText("1")
            txtTextBottom.setText(getString(R.string.txt1_tutorial_listen))
            txtNumberBottom.isGone = false
            txtTextBottom.isGone = false
            txtOne.isGone = false
            stopAnimation(txtTwo)
            startAnimation(txtOne, R.anim.zoom_in)
        } else if (this.status == 1 && next || this.status == 3 && !next) {
            this.status = 2
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("2")
            txtTextTop.setText(getString(R.string.txt2_tutorial_speak_and_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtTwo.isGone = false
            stopAnimation(txtOne)
            stopAnimation(txtThree)
            startAnimation(txtTwo, R.anim.zoom_in)
        } else if (this.status == 2 || this.status == 4 && !next) {
            this.status = 3
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("3")
            txtTextTop.setText(getString(R.string.txt3_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtThree.isGone = false
            stopAnimation(txtTwo)
            stopAnimation(txtFour)
            startAnimation(txtThree, R.anim.zoom_in)
        } else if (this.status == 3 || this.status == 5 && !next) {
            this.status = 4
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("4")
            txtTextTop.setText(getString(R.string.txt4_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            stopAnimation(txtThree)
            stopAnimation(txtFour)
            startAnimation(txtFour, R.anim.zoom_in)
        } else if (this.status == 4 || this.status == 6 && !next) {
            this.status = 5
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("5")
            txtTextTop.setText(getString(R.string.txt5_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("5")
            btnPlay.setImageResource(R.drawable.stop_cv)
            stopAnimation(txtFour)
            startAnimation(txtFour, R.anim.zoom_in)
        } else if (this.status == 5 || this.status == 7 && !next) {
            this.status = 6
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("6")
            txtTextTop.setText(getString(R.string.txt6_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("6")
            btnPlay.setImageResource(R.drawable.listen2_cv)
            btnYes.isGone = false
            btnNo.isGone = false
            stopAnimation(txtFour)
            stopAnimation(txtSeven)
            startAnimation(txtFour, R.anim.zoom_in)
        } else if (this.status == 6 || this.status == 8 && !next) {
            this.status = 7
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("7")
            txtTextTop.setText(getString(R.string.txt7_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtSeven.isGone = false
            btnPlay.setImageResource(R.drawable.listen2_cv)
            btnYes.isGone = false
            btnNo.isGone = false
            stopAnimation(txtFour)
            stopAnimation(txtEight)
            startAnimation(txtSeven, R.anim.zoom_in)
        } else if (this.status == 7 || this.status == 9 && !next) {
            this.status = 8
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("8")
            txtTextTop.setText(getString(R.string.txt8_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtEight.isGone = false
            btnPlay.setImageResource(R.drawable.listen2_cv)
            btnYes.isGone = false
            btnNo.isGone = false
            layoutFirstRunListenNoSmartphone.isGone = true
            stopAnimation(txtSeven)
            stopAnimation(btnReadNowGuidelinesListen)
            startAnimation(txtEight, R.anim.zoom_in)
        } else if (this.status == 8 || this.status == 10 && !next) {
            this.status = 9
            btnNext.setText(getString(R.string.btn_tutorial5))
            layoutFirstRunListenNoSmartphone.isGone = false
            stopAnimation(txtEight)
            startAnimation(btnReadGuidelines, R.anim.zoom_in_first_launch)
            btnReadGuidelines.setOnClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://mzl.la/2Z5OxEQ"))
                startActivity(browserIntent)
            }
        } else if (this.status == 9) {
            getSharedPreferences(FIRST_RUN_LISTEN, PRIVATE_MODE).edit()
                .putBoolean(FIRST_RUN_LISTEN, false).apply()
            firstRunPrefManager.listen = false
            Intent(this, ListenActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }

}