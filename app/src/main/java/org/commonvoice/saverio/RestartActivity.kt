package org.commonvoice.saverio

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.schedule

class RestartActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val UI_LANGUAGE_CHANGED = "UI_LANGUAGE_CHANGED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restart)

        val img: ImageView = this.findViewById(R.id.imgIconStart)
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.start)
        img.startAnimation(animation)

        val sharedPref: SharedPreferences = getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE)
        var restart: Boolean = sharedPref.getBoolean(UI_LANGUAGE_CHANGED, true)
        if (restart) {
            Timer("Restart", false).schedule(1000) {
                restart()
            }
        } else {
            Timer("Start", false).schedule(500) {
                start()
            }
        }
    }

    override fun onBackPressed() {
        //
    }

    fun restart() {
        val sharedPref: SharedPreferences = getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putBoolean(UI_LANGUAGE_CHANGED, false)
        editor.apply()

        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun start() {
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        val sharedPref2: SharedPreferences = newBase.getSharedPreferences("LANGUAGE", 0)
        var tempLang = sharedPref2.getString("LANGUAGE", "en")
        var lang = tempLang.split("-")[0]
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
        configuration.locales = localeList
        return createConfigurationContext(configuration)
    }
}