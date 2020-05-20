package org.commonvoice.saverio.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import org.commonvoice.saverio.TranslationsLanguages
import org.commonvoice.saverio_lib.utils.PrefManager
import org.koin.android.ext.android.inject
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

    val prefManager: PrefManager by inject()

    override fun attachBaseContext(newBase: Context) {
        val tempLang = prefManager.language
        var lang = tempLang.split("-")[0]
        val langSupportedYesOrNot = TranslationsLanguages()
        if (!langSupportedYesOrNot.isSupported(lang)) {
            lang = langSupportedYesOrNot.getDefaultLanguage()
        }
        super.attachBaseContext(newBase.wrap(Locale(lang)))
    }

    private fun Context.wrap(desiredLocale: Locale): Context {
        return when(Build.VERSION.SDK_INT) {
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

}