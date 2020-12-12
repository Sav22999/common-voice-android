package org.commonvoice.saverio.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import org.commonvoice.saverio.TranslationsLanguages
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import kotlinx.coroutines.*
import org.commonvoice.saverio_lib.preferences.LogPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

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

    override fun attachBaseContext(newBase: Context) {
        val tempLang = mainPrefManager.language
        var lang = tempLang.split("-")[0]
        val langSupportedYesOrNot = TranslationsLanguages()
        if (!langSupportedYesOrNot.isSupported(lang)) {
            lang = langSupportedYesOrNot.getDefaultLanguage()
        }
        super.attachBaseContext(newBase.wrap(Locale(lang)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (logPrefManager.saveLogFile) {
            Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
                //Catch exception

                CoroutineScope(Dispatchers.Default).launch {
                    Timber.e(paramThrowable)
                    delay(1_500)
                    android.os.Process.killProcess(android.os.Process.myPid())
                    exitProcess(10)
                }
            }
        }
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
        configuration.locales = localeList
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

}