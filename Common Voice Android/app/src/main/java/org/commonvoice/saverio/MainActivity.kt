package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity() {

    private val translations_languages: Array<String> = arrayOf("en", "it", "sv") //change manually

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


    var languagesListShortArray =
        arrayOf("en") // don't change manually -> it's imported from strings.xml
    var languagesListArray =
        arrayOf("English") // don't change manually -> it's imported from strings.xml
    var selectedLanguageVar = ""
    var logged: Boolean = false
    var userId: String = ""
    var userName: String = ""

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

        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        this.firstRun = sharedPref.getBoolean(PREF_NAME, true)

        // import languages from array
        this.languagesListArray = resources.getStringArray(R.array.languages)
        this.languagesListShortArray = resources.getStringArray(R.array.languages_short)

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selectedLanguageVar = sharedPref2.getString(LANGUAGE_NAME, "en")

        setLanguageUI()

        val sharedPref3: SharedPreferences = getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
        this.logged = sharedPref3.getBoolean(LOGGED_IN_NAME, false)

        if (logged) {
            val sharedPref4: SharedPreferences = getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
            this.userId = sharedPref4.getString(USER_CONNECT_ID, "")

            val sharedPref5: SharedPreferences = getSharedPreferences(USER_NAME, PRIVATE_MODE)
            this.userName = sharedPref5.getString(USER_NAME, "")
        }

        if (this.firstRun) {
            // close main and open tutorial -- first run
            openTutorial()
        } else {
            checkPermissions()
        }
    }

    fun getHiUsernameLoggedIn(): String {
        val sharedPref3: SharedPreferences = getSharedPreferences(LOGGED_IN_NAME, PRIVATE_MODE)
        this.logged = sharedPref3.getBoolean(LOGGED_IN_NAME, false)

        if (logged) {
            val sharedPref4: SharedPreferences = getSharedPreferences(USER_CONNECT_ID, PRIVATE_MODE)
            this.userId = sharedPref4.getString(USER_CONNECT_ID, "")

            val sharedPref5: SharedPreferences = getSharedPreferences(USER_NAME, PRIVATE_MODE)
            this.userName = sharedPref5.getString(USER_NAME, "")
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
        var sharedPref: SharedPreferences? = null
        var returnStatistics: String = "?"
        try {
            if (type == "you") {
                sharedPref = getSharedPreferences(LAST_STATS_YOU, PRIVATE_MODE)
                returnStatistics = sharedPref.getString(LAST_STATS_YOU, "?")
            } else if (type == "everyone") {
                sharedPref = getSharedPreferences(LAST_STATS_EVERYONE, PRIVATE_MODE)
                returnStatistics = sharedPref.getString(LAST_STATS_EVERYONE, "?")
            } else if (type == "voices_now") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_NOW, PRIVATE_MODE)
                returnStatistics = sharedPref.getString(LAST_VOICES_ONLINE_NOW, "?")
            } else if (type == "voices_now") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_BEFORE, PRIVATE_MODE)
                returnStatistics = sharedPref.getString(LAST_VOICES_ONLINE_BEFORE, "?")
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
        return returnStatistics
    }

    fun setSavedStatistics(type: String, statistics: String) {
        try {
            var sharedPref: SharedPreferences? = null
            if (type == "you") {
                sharedPref = getSharedPreferences(LAST_STATS_YOU, PRIVATE_MODE)
                val editor = sharedPref.edit()
                editor.putString(LAST_STATS_YOU, statistics)
                editor.apply()
            } else if (type == "everyone") {
                sharedPref = getSharedPreferences(LAST_STATS_EVERYONE, PRIVATE_MODE)
                val editor = sharedPref.edit()
                editor.putString(LAST_STATS_EVERYONE, statistics)
                editor.apply()
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getSavedStatisticsValue(type: String, index: Int): String {
        var sharedPref: SharedPreferences? = null
        var returnStatistics: String = "?"
        try {
            if (type == "you") {
                if (index == 0) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_0, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_YOU_VALUE_0, "?")
                } else if (index == 1) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_1, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_YOU_VALUE_1, "?")
                } else if (index == 2) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_2, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_YOU_VALUE_2, "?")
                } else if (index == 3) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_3, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_YOU_VALUE_3, "?")
                }
            } else if (type == "everyone") {
                if (index == 0) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_0, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_EVERYONE_VALUE_0, "?")
                } else if (index == 1) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_1, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_EVERYONE_VALUE_1, "?")
                } else if (index == 2) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_2, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_EVERYONE_VALUE_2, "?")
                } else if (index == 3) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_3, PRIVATE_MODE)
                    returnStatistics = sharedPref.getString(LAST_STATS_EVERYONE_VALUE_3, "?")
                }
            }
            //println(" --> "+type+" "+index+" "+returnStatistics)
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
        return returnStatistics
    }

    fun setSavedStatisticsValue(type: String, value: String, index: Int) {
        var sharedPref: SharedPreferences? = null
        var valueToSave = "?"
        if (value != "-1") {
            valueToSave = value
        }
        //println(" --> "+type+" "+index+" "+value+" "+valueToSave)
        try {
            if (type == "you") {
                if (index == 0) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_0, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_YOU_VALUE_0, valueToSave)
                    editor.apply()
                } else if (index == 1) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_1, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_YOU_VALUE_1, valueToSave)
                    editor.apply()
                } else if (index == 2) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_2, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_YOU_VALUE_2, valueToSave)
                    editor.apply()
                } else if (index == 3) {
                    sharedPref = getSharedPreferences(LAST_STATS_YOU_VALUE_3, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_YOU_VALUE_3, valueToSave)
                    editor.apply()
                }
            } else if (type == "everyone") {
                if (index == 0) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_0, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_EVERYONE_VALUE_0, valueToSave)
                    editor.apply()
                } else if (index == 1) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_1, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_EVERYONE_VALUE_1, valueToSave)
                    editor.apply()
                } else if (index == 2) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_2, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_EVERYONE_VALUE_2, valueToSave)
                    editor.apply()
                } else if (index == 3) {
                    sharedPref = getSharedPreferences(LAST_STATS_EVERYONE_VALUE_3, PRIVATE_MODE)
                    val editor = sharedPref.edit()
                    editor.putString(LAST_STATS_EVERYONE_VALUE_3, valueToSave)
                    editor.apply()
                }
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getSavedVoicesOnline(type: String): String {
        var sharedPref: SharedPreferences? = null
        var returnVoicesOnline: String = "?"
        try {
            if (type == "voicesNow") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_NOW, PRIVATE_MODE)
                returnVoicesOnline = sharedPref.getString(LAST_VOICES_ONLINE_NOW, "?")
            } else if (type == "voicesBefore") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_BEFORE, PRIVATE_MODE)
                returnVoicesOnline = sharedPref.getString(LAST_VOICES_ONLINE_BEFORE, "?")
            } else if (type == "voicesNowValue") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_NOW_VALUE, PRIVATE_MODE)
                returnVoicesOnline = sharedPref.getString(LAST_VOICES_ONLINE_NOW_VALUE, "?")
            } else if (type == "voicesBeforeValue") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_BEFORE_VALUE, PRIVATE_MODE)
                returnVoicesOnline = sharedPref.getString(LAST_VOICES_ONLINE_BEFORE_VALUE, "?")
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
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_NOW, PRIVATE_MODE)
                val editor = sharedPref.edit()
                editor.putString(LAST_VOICES_ONLINE_NOW, voices)
                editor.apply()
            } else if (type == "voicesBefore") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_BEFORE, PRIVATE_MODE)
                val editor = sharedPref.edit()
                editor.putString(LAST_VOICES_ONLINE_BEFORE, voices)
                editor.apply()
            } else if (type == "voicesNowValue") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_NOW_VALUE, PRIVATE_MODE)
                val editor = sharedPref.edit()
                editor.putString(LAST_VOICES_ONLINE_NOW_VALUE, voices)
                editor.apply()
            } else if (type == "voicesBeforeValue") {
                sharedPref = getSharedPreferences(LAST_VOICES_ONLINE_BEFORE_VALUE, PRIVATE_MODE)
                val editor = sharedPref.edit()
                editor.putString(LAST_VOICES_ONLINE_BEFORE_VALUE, voices)
                editor.apply()
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun setLanguageSettings(lang: String) {
        try {
            val sharedPref: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
            val editor = sharedPref.edit()
            editor.putString(LANGUAGE_NAME, lang)
            editor.apply()

            var languageChanged = false
            if (this.selectedLanguageVar != lang) {
                languageChanged = true
            }

            this.selectedLanguageVar = lang

            if (languageChanged) {
                Toast.makeText(
                    this,
                    getString(R.string.toast_language_changed).replace(
                        "{{*{{lang}}*}}",
                        this.languagesListArray.get(this.languagesListShortArray.indexOf(this.getSelectedLanguage()))
                    ),
                    Toast.LENGTH_LONG
                ).show()
                setSavedStatistics("you", "?")
                setSavedStatistics("everyone", "?")
                setSavedVoicesOnline("voicesNow", "?")
                setSavedVoicesOnline("voicesBefore", "?")
            }

            setLanguageUI()

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
        }
        finish()
    }

    fun openSpeakSection() {
        val intent = Intent(this, SpeakActivity::class.java).also {
            startActivity(it)
        }
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

    fun noLoggedInNoStatisticsYou() {
        Toast.makeText(
            this,
            getString(R.string.toastNoLoginNoStatistics),
            Toast.LENGTH_LONG
        ).show()
    }

    fun checkPermissions() {
        try {
            checkRecordVoicePermission()
        } catch (e: Exception) {
            //println(" -->> Exception: " + e.toString())
        }
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                checkStoragePermission()
            }
        } catch (e: Exception) {
            //println(" -->> Exception: " + e.toString())
        }
    }

    fun checkRecordVoicePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_REQUEST_CODE
            )
        }
    }

    fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions()
                }
            }
        }
    }

    fun setLanguageUI() {
        var lang = selectedLanguageVar.split("-")[0]
        var restart = false
        if (getString(R.string.language) != selectedLanguageVar && translations_languages.indexOf(lang) != -1) {
            restart = true
        }
        else if (translations_languages.indexOf(lang) == -1 && getString(R.string.language) != "en") {
            restart = true
            lang = "en"
        }
        println("-->sel: " + selectedLanguageVar + " -->lang: " + getString(R.string.language))
        println("-->index: " + translations_languages.indexOf(lang))
        var locale: Locale = Locale(lang)
        Locale.setDefault(locale)
        var res: Resources = resources
        var config: Configuration = res.configuration
        config.setLocale(locale)
        res.updateConfiguration(config, res.displayMetrics)
        //createConfigurationContext(config)

        if (restart) {
            val intent = Intent(this, MainActivity::class.java).also {
                startActivity(it)

                finish()
            }
        }
    }
}
