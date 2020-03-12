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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.schedule

class RestartActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val LANGUAGE_NAME = "LANGUAGE"
    private val UI_LANGUAGE_CHANGED = "UI_LANGUAGE_CHANGED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restart)

        val img: ImageView = this.findViewById(R.id.imgIconStart)
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.start)
        img.startAnimation(animation)

        var restart: Boolean = getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE).getBoolean(
            UI_LANGUAGE_CHANGED,
            true
        )
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
        getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE).edit()
            .putBoolean(UI_LANGUAGE_CHANGED, false).apply()

        val intent = Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun start() {
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //Android 6.0
        } else {
            var tempLang = newBase.getSharedPreferences("LANGUAGE", 0).getString("LANGUAGE", "en")
            var lang = tempLang.split("-")[0]
            val langSupportedYesOrNot = TranslationsLanguages()
            if (!langSupportedYesOrNot.isSupported(lang)) {
                lang = langSupportedYesOrNot.getDefaultLanguage()
            }
            super.attachBaseContext(newBase.wrap(Locale(lang)))
        }
    }

    fun Context.wrap(desiredLocale: Locale): Context {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            getUpdatedContextApi23(desiredLocale)
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N)
            getUpdatedContextApi24(desiredLocale)
        else
            getUpdatedContextApi25(desiredLocale)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun Context.getUpdatedContextApi23(locale: Locale): Context {
        print("\n\n>>-->>\n\n")
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