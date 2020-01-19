package org.commonvoice.saverio

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.extensions.LayoutContainer
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity() {
    private var firstRun = true
    private val RECORD_REQUEST_CODE = 101
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "FIRST_RUN"
    private val LANGUAGE_NAME = "LANGUAGE"
    private val LOGGED_IN_NAME = "LOGGED" //false->no logged-in || true -> logged-in
    private val USER_CONNECT_ID = "USER_CONNECT_ID"
    private val USER_NAME = "USERNAME"
    private val LAST_STATS_EVERYONE = "LAST_STATS_EVERYONE" //yyyy/mm/dd hh:mm:ss
    private val LAST_STATS_YOU = "LAST_STATS_YOU" //yyyy/mm/dd hh:mm:ss
    private val LAST_STATS_EVERYONE_VALUE_0 = "LAST_STATS_EVERYONE_VALUE_0"
    private val LAST_STATS_EVERYONE_VALUE_1 = "LAST_STATS_EVERYONE_VALUE_1"
    private val LAST_STATS_EVERYONE_VALUE_2 = "LAST_STATS_EVERYONE_VALUE_2"
    private val LAST_STATS_EVERYONE_VALUE_3 = "LAST_STATS_EVERYONE_VALUE_3"
    private val LAST_STATS_YOU_VALUE_0 = "LAST_STATS_YOU_VALUE_0"
    private val LAST_STATS_YOU_VALUE_1 = "LAST_STATS_YOU_VALUE_1"
    private val LAST_STATS_YOU_VALUE_2 = "LAST_STATS_YOU_VALUE_2"
    private val LAST_STATS_YOU_VALUE_3 = "LAST_STATS_YOU_VALUE_3"
    private val LAST_VOICES_ONLINE_NOW = "LAST_VOICES_ONLINE_NOW"
    private val LAST_VOICES_ONLINE_BEFORE = "LAST_VOICES_ONLINE_BEFORE"
    private val LAST_VOICES_ONLINE_NOW_VALUE = "LAST_VOICES_ONLINE_NOW_VALUE"
    private val LAST_VOICES_ONLINE_BEFORE_VALUE = "LAST_VOICES_ONLINE_BEFORE_VALUE"
    private val UI_LANGUAGE_CHANGED = "UI_LANGUAGE_CHANGED"
    private val UI_LANGUAGE_CHANGED2 = "UI_LANGUAGE_CHANGED2"
    private val AUTO_PLAY_CLIPS = "AUTO_PLAY_CLIPS"
    private val TODAY_CONTRIBUTING =
        "TODAY_CONTRIBUTING" //saved as "yyyy/mm/dd, n_recorded, n_validated"
    private val DARK_THEME = "DARK_THEME"

    var dashboard_selected = false

    var languagesListShortArray =
        arrayOf("en") // don't change manually -> it's imported from strings.xml
    var languagesListArray =
        arrayOf("English") // don't change manually -> it's imported from strings.xml
    var selectedLanguageVar = "en"
    var logged: Boolean = false
    var userId: String = ""
    var userName: String = ""
    var darkTheme: Boolean = false
    var theme: DarkLightTheme = DarkLightTheme()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        checkConnection()

        this.firstRun = getSharedPreferences(PREF_NAME, PRIVATE_MODE).getBoolean(PREF_NAME, true)

        this.selectedLanguageVar =
            getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE).getString(LANGUAGE_NAME, "en")

        // import languages from array
        this.languagesListArray = resources.getStringArray(R.array.languages)
        this.languagesListShortArray = resources.getStringArray(R.array.languages_short)

        this.darkTheme =
            getSharedPreferences(DARK_THEME, PRIVATE_MODE).getBoolean(DARK_THEME, false)

        if (this.firstRun) {
            // close main and open tutorial -- first run
            openTutorial()
        } else {
            setLanguageUI("start")
            //checkPermissions()
        }

        this.logged =
            getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE).getBoolean(LOGGED_IN_NAME, false)

        if (logged) {
            this.userId =
                getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE).getString(USER_CONNECT_ID, "")

            this.userName = getSharedPreferences(USER_NAME, PRIVATE_MODE).getString(USER_NAME, "")
        }
    }

    fun getHiUsernameLoggedIn(): String {
        this.logged =
            getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE).getBoolean(LOGGED_IN_NAME, false)

        if (logged) {
            this.userId =
                getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE).getString(USER_CONNECT_ID, "")
            this.userName = getSharedPreferences(USER_NAME, PRIVATE_MODE).getString(USER_NAME, "")
        }

        if (this.userName == "") {
            return getString(R.string.text_hi_username) + "!"
        } else {
            return getString(R.string.text_hi_username) + ", " + userName + "!"
        }
    }

    fun getLanguage() {
        Toast.makeText(
            this,
            "Language: " + this.selectedLanguageVar + " index: " + languagesListShortArray.indexOf(
                this.selectedLanguageVar
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    fun getSavedStatistics(type: String): String {
        var returnStatistics: String = "?"
        try {
            if (type == "you") {
                returnStatistics = getSharedPreferences(LAST_STATS_YOU, PRIVATE_MODE).getString(
                    LAST_STATS_YOU,
                    "?"
                )
            } else if (type == "everyone") {
                returnStatistics =
                    getSharedPreferences(LAST_STATS_EVERYONE, PRIVATE_MODE).getString(
                        LAST_STATS_EVERYONE,
                        "?"
                    )
            } else if (type == "voices_now") {
                returnStatistics =
                    getSharedPreferences(LAST_VOICES_ONLINE_NOW, PRIVATE_MODE).getString(
                        LAST_VOICES_ONLINE_NOW,
                        "?"
                    )
            } else if (type == "voices_now") {
                returnStatistics =
                    getSharedPreferences(LAST_VOICES_ONLINE_BEFORE, PRIVATE_MODE).getString(
                        LAST_VOICES_ONLINE_BEFORE,
                        "?"
                    )
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
        return returnStatistics
    }

    fun setDarkThemeSwitch(status: Boolean) {
        if (status != theme.getTheme(this)) {
            if (status) {
                this.showMessage(getString(R.string.toast_dark_theme_on))
            } else {
                this.showMessage(getString(R.string.toast_dark_theme_off))
            }
            theme.setTheme(this, status)
        }
    }

    fun setSavedStatistics(type: String, statistics: String) {
        try {
            if (type == "you") {
                getSharedPreferences(LAST_STATS_YOU, PRIVATE_MODE).edit()
                    .putString(LAST_STATS_YOU, statistics).apply()
            } else if (type == "everyone") {
                getSharedPreferences(LAST_STATS_EVERYONE, PRIVATE_MODE).edit()
                    .putString(LAST_STATS_EVERYONE, statistics).apply()
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getSavedStatisticsValue(type: String, index: Int): String {
        var returnStatistics: String = "?"
        try {
            if (type == "you") {
                if (index == 0) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_YOU_VALUE_0, PRIVATE_MODE).getString(
                            LAST_STATS_YOU_VALUE_0,
                            "?"
                        )
                } else if (index == 1) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_YOU_VALUE_1, PRIVATE_MODE).getString(
                            LAST_STATS_YOU_VALUE_1,
                            "?"
                        )
                } else if (index == 2) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_YOU_VALUE_2, PRIVATE_MODE).getString(
                            LAST_STATS_YOU_VALUE_2,
                            "?"
                        )
                } else if (index == 3) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_YOU_VALUE_3, PRIVATE_MODE).getString(
                            LAST_STATS_YOU_VALUE_3,
                            "?"
                        )
                }
            } else if (type == "everyone") {
                if (index == 0) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_EVERYONE_VALUE_0, PRIVATE_MODE).getString(
                            LAST_STATS_EVERYONE_VALUE_0,
                            "?"
                        )
                } else if (index == 1) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_EVERYONE_VALUE_1, PRIVATE_MODE).getString(
                            LAST_STATS_EVERYONE_VALUE_1,
                            "?"
                        )
                } else if (index == 2) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_EVERYONE_VALUE_2, PRIVATE_MODE).getString(
                            LAST_STATS_EVERYONE_VALUE_2,
                            "?"
                        )
                } else if (index == 3) {
                    returnStatistics =
                        getSharedPreferences(LAST_STATS_EVERYONE_VALUE_3, PRIVATE_MODE).getString(
                            LAST_STATS_EVERYONE_VALUE_3,
                            "?"
                        )
                }
            }
            //println(" --> "+type+" "+index+" "+returnStatistics)
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
        return returnStatistics
    }

    fun setSavedStatisticsValue(type: String, value: String, index: Int) {
        var valueToSave = "?"
        if (value != "-1") {
            valueToSave = value
        }
        //println(" --> "+type+" "+index+" "+value+" "+valueToSave)
        try {
            if (type == "you") {
                if (index == 0) {
                    getSharedPreferences(LAST_STATS_YOU_VALUE_0, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_YOU_VALUE_0, valueToSave).apply()
                } else if (index == 1) {
                    getSharedPreferences(LAST_STATS_YOU_VALUE_1, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_YOU_VALUE_1, valueToSave).apply()
                } else if (index == 2) {
                    getSharedPreferences(LAST_STATS_YOU_VALUE_2, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_YOU_VALUE_2, valueToSave).apply()
                } else if (index == 3) {
                    getSharedPreferences(LAST_STATS_YOU_VALUE_3, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_YOU_VALUE_3, valueToSave).apply()
                }
            } else if (type == "everyone") {
                if (index == 0) {
                    getSharedPreferences(LAST_STATS_EVERYONE_VALUE_0, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_EVERYONE_VALUE_0, valueToSave).apply()
                } else if (index == 1) {
                    getSharedPreferences(LAST_STATS_EVERYONE_VALUE_1, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_EVERYONE_VALUE_1, valueToSave).apply()
                } else if (index == 2) {
                    getSharedPreferences(LAST_STATS_EVERYONE_VALUE_2, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_EVERYONE_VALUE_2, valueToSave).apply()
                } else if (index == 3) {
                    getSharedPreferences(LAST_STATS_EVERYONE_VALUE_3, PRIVATE_MODE).edit()
                        .putString(LAST_STATS_EVERYONE_VALUE_3, valueToSave).apply()
                }
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getSavedVoicesOnline(type: String): String {
        var returnVoicesOnline: String = "?"
        try {
            if (type == "voicesNow") {
                returnVoicesOnline =
                    getSharedPreferences(LAST_VOICES_ONLINE_NOW, PRIVATE_MODE).getString(
                        LAST_VOICES_ONLINE_NOW,
                        "?"
                    )
            } else if (type == "voicesBefore") {
                returnVoicesOnline =
                    getSharedPreferences(LAST_VOICES_ONLINE_BEFORE, PRIVATE_MODE).getString(
                        LAST_VOICES_ONLINE_BEFORE,
                        "?"
                    )
            } else if (type == "voicesNowValue") {
                returnVoicesOnline =
                    getSharedPreferences(LAST_VOICES_ONLINE_NOW_VALUE, PRIVATE_MODE).getString(
                        LAST_VOICES_ONLINE_NOW_VALUE,
                        "?"
                    )
            } else if (type == "voicesBeforeValue") {
                returnVoicesOnline =
                    getSharedPreferences(LAST_VOICES_ONLINE_BEFORE_VALUE, PRIVATE_MODE).getString(
                        LAST_VOICES_ONLINE_BEFORE_VALUE,
                        "?"
                    )
            }
        } catch (e: Exception) {
            println("Error: " + e.toString())
        }
        println(type + " -> " + returnVoicesOnline)
        return returnVoicesOnline
    }

    fun setSavedVoicesOnline(type: String, voices: String) {
        try {
            var sharedPref: SharedPreferences? = null
            if (type == "voicesNow") {
                getSharedPreferences(LAST_VOICES_ONLINE_NOW, PRIVATE_MODE).edit()
                    .putString(LAST_VOICES_ONLINE_NOW, voices).apply()
            } else if (type == "voicesBefore") {
                getSharedPreferences(LAST_VOICES_ONLINE_BEFORE, PRIVATE_MODE).edit()
                    .putString(LAST_VOICES_ONLINE_BEFORE, voices).apply()
            } else if (type == "voicesNowValue") {
                getSharedPreferences(LAST_VOICES_ONLINE_NOW_VALUE, PRIVATE_MODE).edit()
                    .putString(LAST_VOICES_ONLINE_NOW_VALUE, voices).apply()
            } else if (type == "voicesBeforeValue") {
                getSharedPreferences(LAST_VOICES_ONLINE_BEFORE_VALUE, PRIVATE_MODE).edit()
                    .putString(LAST_VOICES_ONLINE_BEFORE_VALUE, voices).apply()
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun setLanguageSettings(lang: String) {
        try {
            getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE).edit().putString(LANGUAGE_NAME, lang)
                .apply()

            var languageChanged = false
            if (this.selectedLanguageVar != lang) {
                languageChanged = true
            }

            this.selectedLanguageVar = lang

            if (languageChanged) {
                getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE).edit()
                    .putBoolean(UI_LANGUAGE_CHANGED, true).apply()

                setLanguageUI("restart")
                setSavedStatistics("you", "?")
                setSavedStatistics("everyone", "?")
                setSavedVoicesOnline("voicesNow", "?")
                setSavedVoicesOnline("voicesBefore", "?")
            }

        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getLanguageList(): ArrayAdapter<String> {
        return ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languagesListArray)
    }

    fun getSelectedLanguage(): String {
        return this.selectedLanguageVar
    }

    fun openTutorial() {
        val intent = Intent(this, TutorialActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun openSpeakSection() {
        /*val intent = Intent(this, SpeakActivity::class.java).also {
            startActivity(it)
        }*/
        openNoAvailableNow()
    }

    fun openListenSection() {
        val intent = Intent(this, ListenActivity::class.java).also {
            startActivity(it)
        }
    }

    fun openLoginSection() {
        val intent = Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            //close the MainActivity
            finish()
        }
    }

    fun openLogoutSection() {
        // logout -> delete USERNAME, USERID e LOGGEDIN variables (shared)
        val intent = Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            //close the MainActivity
            finish()
        }
    }

    fun openNoAvailableNow() {
        val intent = Intent(this, NotAvailableNow::class.java).also {
            startActivity(it)
        }
    }

    fun noLoggedInNoStatisticsYou() {
        Toast.makeText(
            this,
            getString(R.string.toastNoLoginNoStatistics),
            Toast.LENGTH_LONG
        ).show()
    }

    fun checkPermissions() {
        try {
            val PERMISSIONS = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    RECORD_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            //
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions()
                } else {
                    checkPermissions()
                }
            }
        }
    }

    fun setLanguageUI(type: String) {
        var restart: Boolean = getSharedPreferences(UI_LANGUAGE_CHANGED, PRIVATE_MODE).getBoolean(
            UI_LANGUAGE_CHANGED,
            true
        )
        var restart2: Boolean = getSharedPreferences(
            UI_LANGUAGE_CHANGED2,
            PRIVATE_MODE
        ).getBoolean(UI_LANGUAGE_CHANGED2, false)

        //println("-->sel: " + selectedLanguageVar + " -->lang: " + getString(R.string.language))
        //println("-->index: " + translations_languages.indexOf(lang))

        if (restart || type == "restart") {
            getSharedPreferences(UI_LANGUAGE_CHANGED2, PRIVATE_MODE).edit()
                .putBoolean(UI_LANGUAGE_CHANGED2, true).apply()
            val intent = Intent(this, RestartActivity::class.java).also {
                startActivity(it)
            }
            finish()
        } else {
            if (restart2) {
                getSharedPreferences(UI_LANGUAGE_CHANGED2, PRIVATE_MODE).edit()
                    .putBoolean(UI_LANGUAGE_CHANGED2, false).apply()
                showMessage(
                    getString(R.string.toast_language_changed).replace(
                        "{{*{{lang}}*}}",
                        this.languagesListArray.get(this.languagesListShortArray.indexOf(this.getSelectedLanguage()))
                    )
                )
            }
            /*if (type == "start") {
                val intent = Intent(this, RestartActivity::class.java).also {
                    startActivity(it)
                }
            }*/
        }
    }

    fun showMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun checkConnection(): Boolean {
        if (MainActivity.checkInternet(this)) {
            return true
        } else {
            openNoConnection()
            return false
        }
    }

    companion object {
        fun checkInternet(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                //Connection OK
                return true
            } else {
                //No connection
                return false
            }

        }
    }

    fun openNoConnection() {
        val intent = Intent(this, NoConnectionActivity::class.java).also {
            startActivity(it)
        }
    }

    fun startAnimation(img: Button) {
        var animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_out)
        img.startAnimation(animation)
    }

    fun stopAnimation(img: Button) {
        img.clearAnimation()
    }

    fun openWebBrowserForTest() {
        val intent = Intent(this, WebBrowser::class.java).also {
            startActivity(it)
        }
    }

    fun setAutoPlay(status: Boolean) {
        if (status != this.getAutoPlay()) {
            if (status) {
                this.showMessage(getString(R.string.toast_autoplay_clip_on))
            } else {
                this.showMessage(getString(R.string.toast_autoplay_clip_off))
            }
            getSharedPreferences(AUTO_PLAY_CLIPS, PRIVATE_MODE).edit()
                .putBoolean(AUTO_PLAY_CLIPS, status).apply()
        }
    }

    fun getAutoPlay(): Boolean {
        return getSharedPreferences(AUTO_PLAY_CLIPS, PRIVATE_MODE).getBoolean(
            AUTO_PLAY_CLIPS,
            false
        )
    }

    fun getDateToSave(savedDate: String): String {
        var todayDate: String = "?"
        if (Build.VERSION.SDK_INT < 26) {
            val dateTemp = SimpleDateFormat("yyyy/MM/dd")
            todayDate = dateTemp.format(Date()).toString()
        } else {
            val dateTemp = LocalDateTime.now()
            todayDate =
                dateTemp.year.toString() + "/" + dateTemp.monthValue.toString() + "/" + dateTemp.dayOfMonth.toString()
        }
        if (checkDateToday(todayDate, savedDate)) {
            return savedDate
        } else {
            return todayDate
        }
    }

    fun checkDateToday(todayDate: String, savedDate: String): Boolean {
        //true -> savedDate is OK, false -> savedDate is old
        if (todayDate == "?" || savedDate == "?") {
            return false
        } else if (todayDate == savedDate) {
            return true
        } else if (todayDate.split("/")[0] > savedDate.split("/")[0]) {
            return false
        } else if (todayDate.split("/")[1] > savedDate.split("/")[1]) {
            return false
        } else if (todayDate.split("/")[2] > savedDate.split("/")[2]) {
            return false
        } else {
            return true
        }
    }

    fun getContributing(type: String): String {
        //just if the user is logged-in
        if (this.logged) {
            //user logged
            var contributing = getSharedPreferences(TODAY_CONTRIBUTING, PRIVATE_MODE).getString(
                TODAY_CONTRIBUTING,
                "?, ?, ?"
            ).split(", ")
            var dateContributing = contributing[0]
            var dateContributingToSave = getDateToSave(dateContributing)
            var nValidated: String = "?"
            var nRecorded: String = "?"
            if (dateContributingToSave == dateContributing) {
                //same date
                nValidated = contributing[2]
                nRecorded = contributing[1]
                if (nValidated == "?") {
                    nValidated = "0"
                }
                if (nRecorded == "?") {
                    nRecorded = "0"
                }
            } else {
                //new date
                nValidated = "0"
                nRecorded = "0"
            }
            if (type == "validations") {
                return nValidated
            } else if (type == "recordings") {
                return nRecorded
            } else {
                return "?"
            }
        } else {
            //user no logged
        }
        return "?"
    }


    //translation-methods
    override fun attachBaseContext(newBase: Context) {
        var lang = newBase.getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE).getString(
            LANGUAGE_NAME,
            "en"
        ).split("-")[0]
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
