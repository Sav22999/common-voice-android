package org.commonvoice.saverio

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_settings.*
import org.commonvoice.saverio.ui.settings.SettingsFragment
import org.commonvoice.saverio.ui.settings.SettingsViewModel
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var firstRun = true
    private val RECORD_REQUEST_CODE = 101
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "FIRST_RUN"
    private val LANGUAGE_NAME = "LANGUAGE"
    val languages_list_short = arrayOf("it", "en", "fr")
    val languages_list = arrayOf("Italiano", "English")
    var selected_language = ""

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

        val sharedPref2: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        this.selected_language = sharedPref2.getString(LANGUAGE_NAME, "en")

        if (this.firstRun) {
            // close main and open tutorial
            open_tutorial()
        } else {
            checkRecordVoicePermission()
        }

        //get_language()
    }

    fun get_language() {
        Toast.makeText(
            this,
            "Language: " + this.selected_language + " index: " + languages_list_short.indexOf(this.selected_language),
            Toast.LENGTH_LONG
        ).show()
    }

    fun setLanguageSettings(lang: String) {
        val sharedPref: SharedPreferences = getSharedPreferences(LANGUAGE_NAME, PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putString(LANGUAGE_NAME, lang)
        editor.apply()

        this.selected_language = lang
    }

    fun getLanguageList(): ArrayAdapter<String> {
        return ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languages_list)
    }

    fun getSelectedLanguage(): String {
        return this.selected_language
    }

    fun open_tutorial() {
        val intent = Intent(this, TutorialActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    fun open_speak_section() {
        val intent = Intent(this, SpeakActivity::class.java).also {
            startActivity(it)
        }
    }

    fun open_listen_section() {
        val intent = Intent(this, ListenActivity::class.java).also {
            startActivity(it)
        }
    }

    fun open_login_section() {
        val intent = Intent(this, LoginActivity::class.java).also {
            startActivity(it)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    checkRecordVoicePermission()
                }
            }
        }
    }
}
