package org.commonvoice.saverio.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio.utils.TranslationLanguages
import org.commonvoice.saverio_lib.preferences.LogPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

/**
 * An extension of AppCompatActivity which automatically handles language switching
 * by overriding base context.
 */
abstract class VariableLanguageActivity : AppCompatActivity {

    /**
     * Empty constructor
     */
    constructor() : super()

    /**
     * This constructor extends the superclass AppCompatActivity(@LayoutRes layout: Int),
     * which provides automatic inflation of the layout resource, if valid.
     */
    constructor(@LayoutRes layout: Int) : super(layout)

    protected val mainPrefManager: MainPrefManager by inject()
    protected val logPrefManager: LogPrefManager by inject()

    protected val theme: DarkLightTheme by inject()

    override fun attachBaseContext(newBase: Context) {
        val tempLang = mainPrefManager.language
        var lang = tempLang.split("-")[0]
        if (!TranslationLanguages.isSupported(lang)) {
            lang = TranslationLanguages.defaultLanguage
        }
        super.attachBaseContext(newBase.wrap(Locale(lang)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomDefaultUncaughtExceptionHandler()
    }

    private fun Context.wrap(desiredLocale: Locale): Context {
        return when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.M -> { //API 23, the lowest version we support
                getUpdatedContextApi23(desiredLocale)
            }
            Build.VERSION_CODES.N -> { //API 24
                getUpdatedContextApi24(desiredLocale)
            }
            else -> { //API 25 and higher
                getUpdatedContextApi25(desiredLocale)
            }
        }
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
        configuration.setLocales(localeList)
        return createConfigurationContext(configuration)
    }

    protected fun startAnimation(view: View, @AnimRes res: Int) {
        if (mainPrefManager.areAnimationsEnabled) {
            AnimationUtils.loadAnimation(this, res).let {
                view.startAnimation(it)
            }
        }
    }

    protected fun stopAnimation(view: View) {
        view.clearAnimation()
    }

    private fun setCustomDefaultUncaughtExceptionHandler() {
        if (logPrefManager.saveLogFile) {
            val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

            Thread.setDefaultUncaughtExceptionHandler { t, paramThrowable ->
                CoroutineScope(Dispatchers.Default).launch {
                    logPrefManager.stackTrace = paramThrowable.stackTraceToString()
                    logPrefManager.isLogFileSent = false
                    Timber.e(paramThrowable)

                    defaultExceptionHandler?.uncaughtException(t, paramThrowable)
                }
            }
        }
    }
}