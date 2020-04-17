package org.commonvoice.saverio

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import java.util.*


class FirstRunListen : AppCompatActivity() {

    var status: Int = 0
    private var PRIVATE_MODE = 0
    private val FIRST_RUN_LISTEN = "FIRST_RUN_LISTEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_run_listen)

        goNext()
        var btnNext: Button = this.findViewById(R.id.btnNextListen)
        btnNext.setOnClickListener {
            goNext()
        }

        setTheme(this)
    }

    fun setTheme(view: Context) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        theme.setElement(isDark, this.findViewById(R.id.layoutFirstRunListen) as ConstraintLayout)
        theme.setElement(isDark, view, this.findViewById(R.id.btnNextListen) as Button)
    }

    override fun onBackPressed() {
        finish()
    }

    fun goNext() {
        var btnNext: Button = this.findViewById(R.id.btnNextListen)
        var txtNumberBottom: Button = this.findViewById(R.id.btnNumberBottomListen)
        var txtTextBottom: TextView = this.findViewById(R.id.txtTutorialMessageBottomListen)
        var txtNumberTop: Button = this.findViewById(R.id.btnNumberTopListen)
        var txtTextTop: TextView = this.findViewById(R.id.txtTutorialMessageTopListen)
        var txtOne: Button = this.findViewById(R.id.btnOneListen)
        var txtTwo: Button = this.findViewById(R.id.btnTwoListen)
        var txtThree: Button = this.findViewById(R.id.btnThreeListen)
        var txtFour: Button = this.findViewById(R.id.btnFourListen)
        var txtSeven: Button = this.findViewById(R.id.btnSevenListen)
        var txtEight: Button = this.findViewById(R.id.btnEightListen)
        var btnPlay: ImageView = this.findViewById(R.id.imgBtnPlayListen)
        var btnYes: ImageView = this.findViewById(R.id.imgBtnYesListen)
        var btnNo: ImageView = this.findViewById(R.id.imgBtnNoListen)
        if (this.status >= 0 && this.status < 8) {
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

        if (this.status == 0) {
            this.status = 1
            btnNext.setText(getString(R.string.btn_tutorial1))
            txtNumberBottom.setText("1")
            txtTextBottom.setText(getString(R.string.txt1_tutorial_listen))
            txtNumberBottom.isGone = false
            txtTextBottom.isGone = false
            txtOne.isGone = false
            startAnimation(txtOne)
        } else if (this.status == 1) {
            this.status = 2
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("2")
            txtTextTop.setText(getString(R.string.txt2_tutorial_speak_and_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtTwo.isGone = false
            stopAnimation(txtOne)
            startAnimation(txtTwo)
        } else if (this.status == 2) {
            this.status = 3
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("3")
            txtTextTop.setText(getString(R.string.txt3_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtThree.isGone = false
            stopAnimation(txtTwo)
            startAnimation(txtThree)
        } else if (this.status == 3) {
            this.status = 4
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("4")
            txtTextTop.setText(getString(R.string.txt4_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            stopAnimation(txtThree)
            startAnimation(txtFour)
        } else if (this.status == 4) {
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
            startAnimation(txtFour)
        } else if (this.status == 5) {
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
            startAnimation(txtFour)
        } else if (this.status == 6) {
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
            startAnimation(txtSeven)
        } else if (this.status == 7) {
            this.status = 8
            btnNext.setText(getString(R.string.btn_tutorial5))
            txtNumberTop.setText("8")
            txtTextTop.setText(getString(R.string.txt8_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtEight.isGone = false
            btnPlay.setImageResource(R.drawable.listen2_cv)
            btnYes.isGone = false
            btnNo.isGone = false
            stopAnimation(txtSeven)
            startAnimation(txtEight)
        } else if (this.status == 8) {
            getSharedPreferences(FIRST_RUN_LISTEN, PRIVATE_MODE).edit()
                .putBoolean(FIRST_RUN_LISTEN, false).apply()
            val intent = Intent(this, ListenActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }

    fun startAnimation(img: Button) {
        var animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_in)
        img.startAnimation(animation)
    }

    fun stopAnimation(img: Button) {
        img.clearAnimation()
    }

    override fun attachBaseContext(newBase: Context) {
        var tempLang = newBase.getSharedPreferences("LANGUAGE", 0).getString("LANGUAGE", "en")
        var lang = tempLang?.split("-")?.get(0) ?: ""
        val langSupportedYesOrNot = TranslationsLanguages()
        if (!langSupportedYesOrNot.isSupported(lang)) {
            lang = langSupportedYesOrNot.getDefaultLanguage()
        }
        super.attachBaseContext(newBase.wrap(Locale(lang)))
    }

    fun Context.wrap(desiredLocale: Locale): Context {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            return getUpdatedContextApi23(desiredLocale)

        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N)
            getUpdatedContextApi24(desiredLocale)
        else
            getUpdatedContextApi25(desiredLocale)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun Context.getUpdatedContextApi23(locale: Locale): Context {
        val configuration = resources.configuration
        configuration.locale = locale
        return createConfigurationContext(configuration)
    }

    private fun Context.getUpdatedContextApi24(locale: Locale): Context {
        val configuration = resources.configuration
        configuration.setLocale(locale)
        return createConfigurationContext(configuration)
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun Context.getUpdatedContextApi25(locale: Locale): Context {
        val localeList = LocaleList(locale)
        val configuration = resources.configuration
        //configuration.locales = localeList
        return createConfigurationContext(configuration)
    }
}