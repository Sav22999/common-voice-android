package org.commonvoice.saverio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.RecordingsUploadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


class MainActivity : VariableLanguageActivity(R.layout.activity_main) {

    private val workManager: WorkManager by inject()
    private val mainActivityViewModel: MainActivityViewModel by viewModel()

    private val firstRunPrefManager: FirstRunPrefManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()

    companion object {
        const val SOURCE_STORE =
            "GPS" //change this manually -> "n.d.": Not defined, "GPS": Google Play Store, "FD-GH: F-Droid or GitHub

        fun checkInternet(context: Context): Boolean {
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private var firstRun = true
    private val RECORD_REQUEST_CODE = 101
    private val PRIVATE_MODE = 0
    //"LOGGED" //false->no logged-in || true -> logged-in
    //"LAST_STATS_EVERYONE" //yyyy/mm/dd hh:mm:ss
    //"LAST_STATS_YOU" //yyyy/mm/dd hh:mm:ss
    //"TODAY_CONTRIBUTING" //saved as "yyyy/mm/dd, n_recorded, n_validated"

    val urlWithoutLang: String =
        "https://voice.allizom.org/api/v1/" //API url (without lang)

    val urlStatistics =
        "https://www.saveriomorelli.com/api/common-voice-android/v2/" //API to send the request for anonymous statistics
    var lastStatisticsSending: String = "?"

    private val settingsSwitchData: HashMap<String, String> =
        hashMapOf(
            "PREF_NAME" to "FIRST_RUN",
            "LOGGED_IN_NAME" to "LOGGED",
            "USER_NAME" to "USERNAME",
            "LAST_STATS_EVERYONE" to "LAST_STATS_EVERYONE",
            "LAST_STATS_YOU" to "LAST_STATS_YOU",
            "LAST_STATS_EVERYONE_VALUE_0" to "LAST_STATS_EVERYONE_VALUE_0",
            "LAST_STATS_EVERYONE_VALUE_1" to "LAST_STATS_EVERYONE_VALUE_1",
            "LAST_STATS_EVERYONE_VALUE_2" to "LAST_STATS_EVERYONE_VALUE_2",
            "LAST_STATS_EVERYONE_VALUE_3" to "LAST_STATS_EVERYONE_VALUE_3",
            "LAST_STATS_YOU_VALUE_0" to "LAST_STATS_YOU_VALUE_0",
            "LAST_STATS_YOU_VALUE_1" to "LAST_STATS_YOU_VALUE_1",
            "LAST_STATS_YOU_VALUE_2" to "LAST_STATS_YOU_VALUE_2",
            "LAST_STATS_YOU_VALUE_3" to "LAST_STATS_YOU_VALUE_3",
            "LAST_VOICES_ONLINE_NOW" to "LAST_VOICES_ONLINE_NOW",
            "LAST_VOICES_ONLINE_NOW_VALUE" to "LAST_VOICES_ONLINE_NOW_VALUE",
            "LAST_VOICES_ONLINE_BEFORE_VALUE" to "LAST_VOICES_ONLINE_BEFORE_VALUE",
            "UI_LANGUAGE_CHANGED" to "UI_LANGUAGE_CHANGED",
            "UI_LANGUAGE_CHANGED2" to "UI_LANGUAGE_CHANGED2",
            "AUTO_PLAY_CLIPS" to "AUTO_PLAY_CLIPS",
            "APP_ANONYMOUS_STATISTICS" to "APP_ANONYMOUS_STATISTICS",
            "TODAY_CONTRIBUTING" to "TODAY_CONTRIBUTING",
            "DARK_THEME" to "DARK_THEME",
            "UNIQUE_USER_ID" to "UNIQUE_USER_ID",
            "DAILY_GOAL" to "DAILY_GOAL",
            "EXPERIMENTAL_FEATURES" to "EXPERIMENTAL_FEATURES",
            "SAVING_LOGS" to "SAVING_LOGS",
            "CHECK_FOR_UPDATES" to "CHECK_FOR_UPDATES",
            "SKIP_RECORDING_CONFIRMATION" to "SKIP_RECORDING_CONFIRMATION",
            "RECORDING_INDICATOR_SOUND" to "RECORDING_INDICATOR_SOUND",
            "ABORT_CONFIRMATION_DIALOGS_SETTINGS" to "ABORT_CONFIRMATION_DIALOGS_SETTINGS",
            "GESTURES" to "GESTURES",
            "REVIEW_ON_PLAY_STORE" to "REVIEW_ON_PLAY_STORE",
            "LEVEL_SAVED" to "LEVEL_SAVED",
            "RECORDINGS_SAVED" to "RECORDINGS_SAVED",
            "VALIDATIONS_SAVED" to "VALIDATIONS_SAVED"
        )

    var isExperimentalFeaturesActived: Boolean? = null

    var uniqueUserId = ""

    var dashboard_selected = false

    var languagesListShortArray =
        arrayOf("en") // don't change manually -> it's imported from strings.xml
    var languagesListArray =
        arrayOf("English") // don't change manually -> it's imported from strings.xml
    var selectedLanguageVar = mainPrefManager.language
    var logged: Boolean = false
    var userId: String = ""
    var userName: String = ""
    var darkTheme: Boolean = false
    var theme: DarkLightTheme = DarkLightTheme()
    var isAbortConfirmation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        //checkConnection()

        this.firstRun =
            getSharedPreferences(settingsSwitchData["PREF_NAME"], PRIVATE_MODE).getBoolean(
                settingsSwitchData["PREF_NAME"],
                true
            )

        // import languages from array
        this.languagesListArray = resources.getStringArray(R.array.languages)
        this.languagesListShortArray = resources.getStringArray(R.array.languages_short)

        this.darkTheme =
            getSharedPreferences(settingsSwitchData["DARK_THEME"], PRIVATE_MODE).getBoolean(
                settingsSwitchData["DARK_THEME"],
                false
            )

        if (this.firstRun) {
            // close main and open tutorial -- first run
            this.openTutorial()
        } else {
            this.setLanguageUI("start")
            //checkPermissions()

            RecordingsUploadWorker.attachToWorkManager(workManager)
            SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
            ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)

            mainActivityViewModel.postStats(BuildConfig.VERSION_NAME, SOURCE_STORE)
        }

        this.checkUserLoggedIn()
        this.resetDashboardData()

        this.checkIfSessionIsExpired()
        this.reviewOnPlayStore()
    }

    fun checkIfSessionIsExpired() {
        //if the userid returns "null", to the user have to log in again
        if (logged) {
            val path = "user_client" //API to get sentences
            val que = Volley.newRequestQueue(this)
            //SystemClock.sleep(1000L);
            val req = object : StringRequest(Request.Method.GET, urlWithoutLang + path,
                Response.Listener {
                    //println("-->> " + it.toString() + " <<--")
                    if (it.toString() != "null") {
                        val jsonResult = it.toString()
                    } else {
                        logoutUser()
                    }
                }, Response.ErrorListener {
                    println(" -->> Something wrong: " + it.toString() + " <<-- ")
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put(
                        "Cookie",
                        "connect.sid=" + userId
                    )
                    return headers
                }
            }
            que.add(req)
        }
    }

    fun logoutUser() {
        showMessageDialog(
            "",
            getString(R.string.message_log_in_again)
        )
        logged = false
        mainPrefManager.sessIdCookie = null
        getSharedPreferences(settingsSwitchData["LOGGED_IN_NAME"], PRIVATE_MODE).edit()
            .putBoolean(settingsSwitchData["LOGGED_IN_NAME"], false).apply()
        getSharedPreferences(settingsSwitchData["USER_NAME"], PRIVATE_MODE).edit()
            .putString(settingsSwitchData["USER_NAME"], "").apply()
        setLevelRecordingsValidations(0, 0)
        setLevelRecordingsValidations(1, 0)
        setLevelRecordingsValidations(2, 0)
        getSharedPreferences(settingsSwitchData["DAILY_GOAL"], PRIVATE_MODE).edit()
            .putInt(settingsSwitchData["DAILY_GOAL"], 0).apply()
        mainActivityViewModel.clearDB()
    }

    fun setLevelRecordingsValidations(type: Int, value: Int) {
        when (type) {
            0 -> {
                //level
                getSharedPreferences(settingsSwitchData["LEVEL_SAVED"], PRIVATE_MODE).edit()
                    .putInt(settingsSwitchData["LEVEL_SAVED"], value).apply()
            }
            1 -> {
                //recordings
                getSharedPreferences(settingsSwitchData["RECORDINGS_SAVED"], PRIVATE_MODE).edit()
                    .putInt(settingsSwitchData["RECORDINGS_SAVED"], value).apply()
            }
            2 -> {
                //validations
                getSharedPreferences(settingsSwitchData["VALIDATIONS_SAVED"], PRIVATE_MODE).edit()
                    .putInt(settingsSwitchData["VALIDATIONS_SAVED"], value).apply()
            }
        }
    }

    fun reviewOnPlayStore() {
        //just if it's the GPS version
        if (getSourceStore() == "GPS") {
            val counter = getSharedPreferences(
                settingsSwitchData["REVIEW_ON_PLAY_STORE"],
                PRIVATE_MODE
            ).getInt(
                settingsSwitchData["REVIEW_ON_PLAY_STORE"],
                0
            )
            val times = 100 //after this times it will show the message
            if (((counter % times) == 0 || (counter % times) == times)) {
                showMessageDialog(
                    "",
                    getString(R.string.message_review_app_on_play_store)
                )
            }
            getSharedPreferences(settingsSwitchData["REVIEW_ON_PLAY_STORE"], PRIVATE_MODE).edit()
                .putInt(
                    settingsSwitchData["REVIEW_ON_PLAY_STORE"],
                    counter + 1
                ).apply()
        }
    }

    fun getSourceStore(): String {
        return MainActivity.SOURCE_STORE
    }

    fun checkNewVersionAvailable(forcedCheck: Boolean = false) {
        if (this.getCheckForUpdatesSwitch() or forcedCheck == true) {
            try {
                val urlApiGithub =
                    "https://api.github.com/repos/Sav22999/common-voice-android/releases/latest"
                val que = Volley.newRequestQueue(this)
                val req = object : StringRequest(Request.Method.GET, urlApiGithub,
                    Response.Listener {
                        //println("-->> " + it.toString() + " <<--")
                        val currentVersion: String = BuildConfig.VERSION_NAME
                        var serverVersion: String = currentVersion
                        val jsonResult = it.toString()
                        if (jsonResult.length > 2) {
                            try {
                                val jsonObj = JSONObject(
                                    jsonResult.substring(
                                        jsonResult.indexOf("{"),
                                        jsonResult.lastIndexOf("}") + 1
                                    )
                                )
                                serverVersion = jsonObj.getString("tag_name")
                                val code: String = serverVersion.replace(".", "_")
                                //println(">> current: " + currentVersion + " - new: " + serverVersion)
                                if (currentVersion != serverVersion) {
                                    if (!getSharedPreferences(
                                            "NEW_VERSION_" + code,
                                            PRIVATE_MODE
                                        ).getBoolean(
                                            "NEW_VERSION_" + code,
                                            false
                                        )
                                    ) {
                                        showMessageDialog(
                                            "",
                                            getString(R.string.message_dialog_new_version_available).replace(
                                                "{{*{{n_version}}*}}",
                                                serverVersion
                                            )
                                        )
                                        getSharedPreferences(
                                            "NEW_VERSION_" + code,
                                            PRIVATE_MODE
                                        ).edit()
                                            .putBoolean("NEW_VERSION_" + code, true).apply()
                                    }
                                }
                            } catch (e: Exception) {
                                println(" -->> Something wrong: " + it.toString() + " <<-- ")
                            }
                        }
                    }, Response.ErrorListener {
                        println(" -->> Something wrong: " + it.toString() + " <<-- ")
                    }
                ) {}
                que.add(req)
            } catch (e: Exception) {
                println(" -->> Something wrong: " + e.toString() + " <<-- ")
            }
        }
    }

    fun getHiUsernameLoggedIn(): String {
        this.logged =
            getSharedPreferences(settingsSwitchData["LOGGED_IN_NAME"], PRIVATE_MODE).getBoolean(
                settingsSwitchData["LOGGED_IN_NAME"],
                false
            )

        if (logged) {
            this.userId = mainPrefManager.sessIdCookie!!
            this.userName =
                getSharedPreferences(settingsSwitchData["USER_NAME"], PRIVATE_MODE).getString(
                    settingsSwitchData["USER_NAME"],
                    ""
                )!!
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

    fun checkUserLoggedIn() {
        this.logged =
            getSharedPreferences(settingsSwitchData["LOGGED_IN_NAME"], PRIVATE_MODE).getBoolean(
                settingsSwitchData["LOGGED_IN_NAME"],
                false
            )

        if (logged) {
            this.userId = mainPrefManager.sessIdCookie ?: ""

            this.userName =
                getSharedPreferences(settingsSwitchData["USER_NAME"], PRIVATE_MODE).getString(
                    settingsSwitchData["USER_NAME"],
                    ""
                )!!
        }
    }

    fun getSavedStatistics(type: String): String {
        var returnStatistics: String = "?"
        try {
            if (type == "you") {
                returnStatistics = getSharedPreferences(
                    settingsSwitchData["LAST_STATS_YOU"],
                    PRIVATE_MODE
                ).getString(
                    settingsSwitchData["LAST_STATS_YOU"],
                    "?"
                )!!
            } else if (type == "everyone") {
                returnStatistics =
                    getSharedPreferences(
                        settingsSwitchData["LAST_STATS_EVERYONE"],
                        PRIVATE_MODE
                    ).getString(
                        settingsSwitchData["LAST_STATS_EVERYONE"],
                        "?"
                    )!!
            } else if (type == "voices_now") {
                returnStatistics =
                    getSharedPreferences(
                        settingsSwitchData["LAST_VOICES_ONLINE_NOW"],
                        PRIVATE_MODE
                    ).getString(
                        settingsSwitchData["LAST_VOICES_ONLINE_NOW"],
                        "?"
                    )!!
            } else if (type == "voices_now") {
                returnStatistics =
                    getSharedPreferences(
                        settingsSwitchData["LAST_VOICES_ONLINE_BEFORE"],
                        PRIVATE_MODE
                    ).getString(
                        settingsSwitchData["LAST_VOICES_ONLINE_BEFORE"],
                        "?"
                    )!!
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
        return returnStatistics
    }

    fun setDarkThemeSwitch(status: Boolean) {
        if (status != theme.getTheme(this)) {
            if (status) {
                //this.showMessage(getString(R.string.toast_dark_theme_on))
                //EXM02
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_dark_theme_on),
                        type = 2
                    )
                }
            } else {
                //this.showMessage(getString(R.string.toast_dark_theme_off))
                //EXM03
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_dark_theme_off),
                        type = 2
                    )
                }
            }
            theme.setTheme(this, status)
        }
    }

    fun setStatisticsSwitch(status: Boolean) {
        if (status != this.getStatisticsSwitch()) {
            if (status) {
                //EXM07
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_anonymous_statistics_on)
                    )
                }
            } else {
                //EXM08
                //if (!isAbortConfirmation) {
                showMessageDialog(
                    "",
                    getString(R.string.toast_anonymous_statistics_off)
                )
                //}
            }
            getSharedPreferences(
                settingsSwitchData["APP_ANONYMOUS_STATISTICS"],
                PRIVATE_MODE
            ).edit()
                .putBoolean(settingsSwitchData["APP_ANONYMOUS_STATISTICS"], status).apply()
            mainPrefManager.areStatsAnonymous = status
            mainActivityViewModel.postStats(BuildConfig.VERSION_NAME, SOURCE_STORE)
        }
    }

    fun getExperimentalFeaturesSwitch(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["EXPERIMENTAL_FEATURES"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["EXPERIMENTAL_FEATURES"],
            false
        )
    }

    fun getExperimentalFeaturesValue(): Boolean {
        if (this.isExperimentalFeaturesActived == null) {
            this.isExperimentalFeaturesActived = getExperimentalFeaturesSwitch()
        }
        return this.isExperimentalFeaturesActived!!
    }

    fun setExperimentalFeaturesSwitch(status: Boolean) {
        if (status != this.getExperimentalFeaturesSwitch()) {
            if (status) {
                //EXM07
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_experimental_features_on)
                    )
                }
            } else {
                //EXM08
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_experimental_featured_off)
                    )
                }
            }
            getSharedPreferences(
                settingsSwitchData["EXPERIMENTAL_FEATURES"],
                PRIVATE_MODE
            ).edit()
                .putBoolean(settingsSwitchData["EXPERIMENTAL_FEATURES"], status).apply()
        }
    }

    fun getStatisticsSwitch(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["APP_ANONYMOUS_STATISTICS"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["APP_ANONYMOUS_STATISTICS"],
            true
        )
    }

    fun getRecordingIndicatorSoundSwitch(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["RECORDING_INDICATOR_SOUND"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["RECORDING_INDICATOR_SOUND"],
            false
        )
    }

    fun setRecordingIndicatorSoundSwitch(status: Boolean) {
        if (status != this.getRecordingIndicatorSoundSwitch()) {
            if (status) {
                //EXM07
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_recording_indicator_sound_on)
                    )
                }
            } else {
                //EXM08
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_recording_indicator_sound_off)
                    )
                }
            }
            getSharedPreferences(
                settingsSwitchData["RECORDING_INDICATOR_SOUND"],
                PRIVATE_MODE
            ).edit()
                .putBoolean(settingsSwitchData["RECORDING_INDICATOR_SOUND"], status).apply()
        }
    }

    fun getCheckForUpdatesSwitch(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["CHECK_FOR_UPDATES"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["CHECK_FOR_UPDATES"],
            true
        )
    }

    fun setCheckForUpdatesSwitch(status: Boolean) {
        if (status != this.getCheckForUpdatesSwitch()) {
            if (status) {
                //EXM07
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_check_for_updated_on)
                    )
                }
            } else {
                //EXM08
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_check_for_updated_off)
                    )
                }
            }
            getSharedPreferences(settingsSwitchData["CHECK_FOR_UPDATES"], PRIVATE_MODE).edit()
                .putBoolean(settingsSwitchData["CHECK_FOR_UPDATES"], status).apply()
        }
    }

    fun getAbortConfirmationDialogsInSettingsSwitch(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["ABORT_CONFIRMATION_DIALOGS_SETTINGS"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["ABORT_CONFIRMATION_DIALOGS_SETTINGS"],
            false
        )
    }

    fun setAbortConfirmationDialogsInSettingsSwitch(status: Boolean) {
        if (status != this.getAbortConfirmationDialogsInSettingsSwitch()) {
            if (status) {
                //EXM07
                /*
                    showMessageDialog(
                        "",
                        getString(R.string.toast_abort_confirmation_dialogs_in_settings_on)
                    )
                */
            } else {
                //EXM08
                showMessageDialog(
                    "",
                    getString(R.string.toast_abort_confirmation_dialogs_in_settings_off)
                )
            }
            this.isAbortConfirmation = status
            getSharedPreferences(
                settingsSwitchData["ABORT_CONFIRMATION_DIALOGS_SETTINGS"],
                PRIVATE_MODE
            ).edit()
                .putBoolean(settingsSwitchData["ABORT_CONFIRMATION_DIALOGS_SETTINGS"], status)
                .apply()
        }
    }

    fun getGesturesSettingsSwitch(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["GESTURES"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["GESTURES"],
            false
        )
    }

    fun setGesturesSettingsSwitch(status: Boolean) {
        if (status != this.getGesturesSettingsSwitch()) {
            if (status) {
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_gestures_on)
                    )
                }
            } else {
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_gestures_off)
                    )
                }
            }
            getSharedPreferences(
                settingsSwitchData["GESTURES"],
                PRIVATE_MODE
            ).edit()
                .putBoolean(settingsSwitchData["GESTURES"], status)
                .apply()
        }
    }

    fun getSkipRecordingsConfirmationSwitch(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["SKIP_RECORDING_CONFIRMATION"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["SKIP_RECORDING_CONFIRMATION"],
            false
        )
    }

    fun setSkipRecordingsConfirmationSwitch(status: Boolean) {
        if (status != this.getSkipRecordingsConfirmationSwitch()) {
            if (status) {
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_skip_recording_confirmation_on)
                    )
                }
            } else {
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_skip_recording_confirmation_off)
                    )
                }
            }
            getSharedPreferences(
                settingsSwitchData["SKIP_RECORDING_CONFIRMATION"],
                PRIVATE_MODE
            ).edit()
                .putBoolean(settingsSwitchData["SKIP_RECORDING_CONFIRMATION"], status)
                .apply()
        }
    }

    fun setSavedStatistics(type: String, statistics: String) {
        try {
            if (type == "you") {
                getSharedPreferences(settingsSwitchData["LAST_STATS_YOU"], PRIVATE_MODE).edit()
                    .putString(settingsSwitchData["LAST_STATS_YOU"], statistics).apply()
            } else if (type == "everyone") {
                getSharedPreferences(
                    settingsSwitchData["LAST_STATS_EVERYONE"],
                    PRIVATE_MODE
                ).edit()
                    .putString(settingsSwitchData["LAST_STATS_EVERYONE"], statistics).apply()
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getSavedStatisticsValue(type: String, index: Int): String {
        var returnStatistics: String = "?"
        try {
            if (type == "you") {
                when (index) {
                    0 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_0"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_0"],
                                "?"
                            )!!
                    }
                    1 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_1"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_1"],
                                "?"
                            )!!
                    }
                    2 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_2"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_2"],
                                "?"
                            )!!
                    }
                    3 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_3"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_3"],
                                "?"
                            )!!
                    }
                }
            } else if (type == "everyone") {
                when (index) {
                    0 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_0"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_0"],
                                "?"
                            )!!
                    }
                    1 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_1"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_1"],
                                "?"
                            )!!
                    }
                    2 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_2"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_2"],
                                "?"
                            )!!
                    }
                    3 -> {
                        returnStatistics =
                            getSharedPreferences(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_3"],
                                PRIVATE_MODE
                            ).getString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_3"],
                                "?"
                            )!!
                    }
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
                when (index) {
                    0 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_YOU_VALUE_0"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_0"],
                                valueToSave
                            )
                            .apply()
                    }
                    1 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_YOU_VALUE_1"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_1"],
                                valueToSave
                            )
                            .apply()
                    }
                    2 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_YOU_VALUE_2"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_2"],
                                valueToSave
                            )
                            .apply()
                    }
                    3 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_YOU_VALUE_3"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_YOU_VALUE_3"],
                                valueToSave
                            )
                            .apply()
                    }
                }
            } else if (type == "everyone") {
                when (index) {
                    0 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_EVERYONE_VALUE_0"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_0"],
                                valueToSave
                            )
                            .apply()
                    }
                    1 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_EVERYONE_VALUE_1"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_1"],
                                valueToSave
                            )
                            .apply()
                    }
                    2 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_EVERYONE_VALUE_2"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_2"],
                                valueToSave
                            )
                            .apply()
                    }
                    3 -> {
                        getSharedPreferences(
                            settingsSwitchData["LAST_STATS_EVERYONE_VALUE_3"],
                            PRIVATE_MODE
                        ).edit()
                            .putString(
                                settingsSwitchData["LAST_STATS_EVERYONE_VALUE_3"],
                                valueToSave
                            )
                            .apply()
                    }
                }
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getSavedVoicesOnline(type: String): String {
        var returnVoicesOnline: String = "?"
        try {
            when (type) {
                "voicesNow" -> {
                    returnVoicesOnline =
                        getSharedPreferences(
                            settingsSwitchData["LAST_VOICES_ONLINE_NOW"],
                            PRIVATE_MODE
                        ).getString(
                            settingsSwitchData["LAST_VOICES_ONLINE_NOW"],
                            "?"
                        )!!
                }
                "voicesBefore" -> {
                    returnVoicesOnline =
                        getSharedPreferences(
                            settingsSwitchData["LAST_VOICES_ONLINE_BEFORE"],
                            PRIVATE_MODE
                        ).getString(
                            settingsSwitchData["LAST_VOICES_ONLINE_BEFORE"],
                            "?"
                        )!!
                }
                "voicesNowValue" -> {
                    returnVoicesOnline =
                        getSharedPreferences(
                            settingsSwitchData["LAST_VOICES_ONLINE_NOW_VALUE"],
                            PRIVATE_MODE
                        ).getString(
                            settingsSwitchData["LAST_VOICES_ONLINE_NOW_VALUE"],
                            "?"
                        )!!
                }
                "voicesBeforeValue" -> {
                    returnVoicesOnline =
                        getSharedPreferences(
                            settingsSwitchData["LAST_VOICES_ONLINE_BEFORE_VALUE"],
                            PRIVATE_MODE
                        ).getString(
                            settingsSwitchData["LAST_VOICES_ONLINE_BEFORE_VALUE"],
                            "?"
                        )!!
                }
            }
        } catch (e: Exception) {
            println("Error: " + e.toString())
        }
        //println(type + " -> " + returnVoicesOnline)
        return returnVoicesOnline
    }

    fun setSavedVoicesOnline(type: String, voices: String) {
        try {
            var sharedPref: SharedPreferences? = null
            when (type) {
                "voicesNow" -> {
                    getSharedPreferences(
                        settingsSwitchData["LAST_VOICES_ONLINE_NOW"],
                        PRIVATE_MODE
                    ).edit()
                        .putString(settingsSwitchData["LAST_VOICES_ONLINE_NOW"], voices).apply()
                }
                "voicesBefore" -> {
                    getSharedPreferences(
                        settingsSwitchData["LAST_VOICES_ONLINE_BEFORE"],
                        PRIVATE_MODE
                    ).edit()
                        .putString(settingsSwitchData["LAST_VOICES_ONLINE_BEFORE"], voices)
                        .apply()
                }
                "voicesNowValue" -> {
                    getSharedPreferences(
                        settingsSwitchData["LAST_VOICES_ONLINE_NOW_VALUE"],
                        PRIVATE_MODE
                    ).edit()
                        .putString(settingsSwitchData["LAST_VOICES_ONLINE_NOW_VALUE"], voices)
                        .apply()
                }
                "voicesBeforeValue" -> {
                    getSharedPreferences(
                        settingsSwitchData["LAST_VOICES_ONLINE_BEFORE_VALUE"],
                        PRIVATE_MODE
                    ).edit()
                        .putString(
                            settingsSwitchData["LAST_VOICES_ONLINE_BEFORE_VALUE"],
                            voices
                        )
                        .apply()
                }
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun setLanguageSettings(lang: String) {
        try {
            val languageChanged = if (lang != mainPrefManager.language) {
                true
            } else {
                false
            }

            if (languageChanged) {
                mainPrefManager.language = lang
                mainActivityViewModel.clearDB()

                SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
                ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)
            }

            this.selectedLanguageVar = lang

            if (languageChanged) {
                getSharedPreferences(
                    settingsSwitchData["UI_LANGUAGE_CHANGED"],
                    PRIVATE_MODE
                ).edit()
                    .putBoolean(settingsSwitchData["UI_LANGUAGE_CHANGED"], true).apply()

                setLanguageUI("restart")
                resetDashboardData()
            }

        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }
    }

    fun getDailyGoal(): Int {
        return statsPrefManager.dailyGoalObjective
    }

    fun resetDashboardData() {
        setSavedStatistics("you", "?")
        setSavedStatistics("everyone", "?")
        setSavedVoicesOnline("voicesNow", "?")
        setSavedVoicesOnline("voicesBefore", "?")
    }

    fun getLanguageList(): ArrayAdapter<String> {
        return ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            languagesListArray
        )
    }

    fun getSelectedLanguage(): String {
        return this.selectedLanguageVar
    }

    fun openDailyGoalDialog() {
        try {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val message: MessageDialog =
                MessageDialog(this, this, 1, value = getDailyGoal(), width = width, height = height)
            message.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - OPEN DAILY GOAL DIALOG: " + exception.toString() + " --!!")
        }
    }

    fun refreshDailyGoalDataInDashboard() {
        //refresh data of Daily goal in Dashboard
        val goalText = this.findViewById<TextView>(R.id.labelDashboardDailyGoalValue)
        if (getDailyGoal() == 0) {
            goalText.text = getString(R.string.daily_goal_is_not_set)
            goalText.typeface = Typeface.DEFAULT
            this.findViewById<TextView>(R.id.buttonDashboardSetDailyGoal).text =
                getString(R.string.set_daily_goal)
            //println("Daily goal is not set")
        } else {
            goalText.text = getDailyGoal().toString()
            goalText.typeface = ResourcesCompat.getFont(this, R.font.sourcecodepro)
            this.findViewById<TextView>(R.id.buttonDashboardSetDailyGoal).text =
                getString(R.string.edit_daily_goal)
            //println("Daily goal is set")
        }
    }

    fun setDailyGoal(dailyGoalValue: Int = 0) {
        var value = dailyGoalValue
        if (value < 0) value = 0
        statsPrefManager.dailyGoalObjective = value
    }

    fun passedThirtySeconds(
        oldDate: List<String>,
        oldTime: List<String>,
        newDate: List<String>,
        newTime: List<String>
    ): Boolean {
        var returnTrueOrFalse: Boolean = true

        try {
            // the else-clause indicates the "==", because it shouldn't be never old>new
            if (oldDate[0].toInt() < newDate[0].toInt()) {
                returnTrueOrFalse = true
            } else {
                if (oldDate[1].toInt() < newDate[1].toInt()) {
                    returnTrueOrFalse = true
                } else {
                    if (oldTime[0].toInt() < newTime[0].toInt()) {
                        returnTrueOrFalse = true
                    } else {
                        if (oldTime[1].toInt() < newTime[1].toInt()) {
                            if (newTime[1].toInt() - 1 > oldTime[1].toInt()) {
                                returnTrueOrFalse = true
                            } else {
                                returnTrueOrFalse =
                                    newTime[2].toInt() + 60 - oldTime[2].toInt() > 30
                            }
                        } else {
                            if (oldTime[2].toInt() < newTime[2].toInt()) {
                                if (newTime[2].toInt() > 30) {
                                    returnTrueOrFalse = newTime[2].toInt() - oldTime[2].toInt() > 30
                                } else {
                                    returnTrueOrFalse = false
                                }
                            } else {
                                returnTrueOrFalse = false
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            println("Exception: " + ex.toString())
        }
        return returnTrueOrFalse
    }

    fun showMessageDialog(
        title: String,
        text: String,
        errorCode: String = "",
        details: String = "",
        type: Int = 0
    ) {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        //val width = metrics.widthPixels
        val height = metrics.heightPixels
        try {
            var messageText = text
            if (errorCode != "") {
                if (messageText.contains("{{*{{error_code}}*}}")) {
                    messageText = messageText.replace("{{*{{error_code}}*}}", errorCode)
                } else {
                    messageText = messageText + "\n\n[Message Code: EX-" + errorCode + "]"
                }
            }
            val message: MessageDialog =
                MessageDialog(this, type, title, messageText, details = details, height = height)
            message.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
    }

    fun openTutorial() {
        Intent(this, TutorialActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun openSpeakSection() {
        if (firstRunPrefManager.speak) {
            Intent(this, FirstRunSpeak::class.java).also {
                startActivity(it)
            }
        } else {
            Intent(this, SpeakActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    fun openListenSection() {
        if (firstRunPrefManager.listen) {
            Intent(this, FirstRunListen::class.java).also {
                startActivity(it)
            }
        } else {
            Intent(this, ListenActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    fun openLoginSection() {
        //"110" is chosen by me to identify this request
        Intent(this, LoginActivity::class.java).also {
            startActivityForResult(it, 110)
        }
    }

    fun openProfileSection() {
        // if the user logged-in, it shows profile
        //"111" is chosen by me to identify this request
        Intent(this, LoginActivity::class.java).also {
            startActivityForResult(it, 111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 111 || requestCode == 110) && resultCode == RESULT_OK) {
            //println("MainActivity updated")
            //data?.extras.getString("key") //to get "putExtra" information by "key"
            recreate()
        } else {
            //println("MainActivity updated (2)")
            recreate()
        }
    }

    fun openNoAvailableNow() {
        Intent(this, NotAvailableNow::class.java).also {
            startActivity(it)
        }
    }

    fun noLoggedInNoStatisticsYou() {
        //EXM01
        showMessageDialog(
            "",
            getString(R.string.toastNoLoginNoStatistics)
        )
    }

    fun noLoggedInNoDailyGoal() {
        //EXM20
        showMessageDialog(
            "",
            getString(R.string.toastNoLoginNoDailyGoal)
        )
    }

    private fun checkPermissions() {
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

    private fun setLanguageUI(type: String) {
        val restart: Boolean = getSharedPreferences(
            settingsSwitchData["UI_LANGUAGE_CHANGED"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["UI_LANGUAGE_CHANGED"],
            true
        )
        val restart2: Boolean = getSharedPreferences(
            settingsSwitchData["UI_LANGUAGE_CHANGED2"],
            PRIVATE_MODE
        ).getBoolean(settingsSwitchData["UI_LANGUAGE_CHANGED2"], false)

        //println("-->sel: " + selectedLanguageVar + " -->lang: " + getString(R.string.language))
        //println("-->index: " + translations_languages.indexOf(lang))
        val android6 = Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
        if (android6) {
            //Android 6.0
            val tempLang = getSharedPreferences("LANGUAGE", 0).getString("LANGUAGE", "en")
            var lang = tempLang!!.split("-")[0]
            val langSupportedYesOrNot = TranslationsLanguages()
            if (!langSupportedYesOrNot.isSupported(lang)) {
                lang = langSupportedYesOrNot.getDefaultLanguage()
            }
            val locale: Locale = Locale(lang)
            Locale.setDefault(locale)
            val res: Resources = resources
            val config: Configuration = res.configuration
            config.setLocale(locale)
            res.updateConfiguration(config, res.displayMetrics)
        }
        if (restart || type == "restart") {
            getSharedPreferences(
                settingsSwitchData["UI_LANGUAGE_CHANGED2"],
                PRIVATE_MODE
            ).edit()
                .putBoolean(settingsSwitchData["UI_LANGUAGE_CHANGED2"], true).apply()

            if (android6) {
                getSharedPreferences(
                    settingsSwitchData["UI_LANGUAGE_CHANGED"],
                    PRIVATE_MODE
                ).edit()
                    .putBoolean(settingsSwitchData["UI_LANGUAGE_CHANGED"], false).apply()
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                Intent(this, RestartActivity::class.java).also {
                    startActivity(it)
                }
            }
            finish()
        } else {
            if (restart2) {
                getSharedPreferences(
                    settingsSwitchData["UI_LANGUAGE_CHANGED2"],
                    PRIVATE_MODE
                ).edit()
                    .putBoolean(settingsSwitchData["UI_LANGUAGE_CHANGED2"], false).apply()
                /*showMessage(
                        getString(R.string.toast_language_changed).replace(
                            "{{*{{lang}}*}}",
                            this.languagesListArray.get(this.languagesListShortArray.indexOf(this.getSelectedLanguage()))
                        )
                    )*/
                //EXM04
                val tl = TranslationsLanguages()
                var detailsMessage = ""
                if (tl.isUncompleted(this.getSelectedLanguage())) {
                    detailsMessage =
                        "\n" + getString(R.string.message_app_not_completely_translated)
                }
                showMessageDialog(
                    "",
                    getString(R.string.toast_language_changed).replace(
                        "{{*{{lang}}*}}",
                        this.languagesListArray.get(this.languagesListShortArray.indexOf(this.getSelectedLanguage()))
                    ) + detailsMessage
                )
            }
            /*if (type == "start") {
                    Intent(this, RestartActivity::class.java).also {
                        startActivity(it)
                    }
                }*/
        }
    }

    fun showMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun checkConnection(): Boolean {
        if (MainActivity.checkInternet(this)) {
            return true
        } else {
            openNoConnection()
            return false
        }
    }

    fun openNoConnection() {
        /*Intent(this, NoConnectionActivity::class.java).also {
            startActivity(it)
        }*/
    }

    fun startAnimation(img: Button) {
        val animation: Animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_out)
        img.startAnimation(animation)
    }

    fun stopAnimation(img: Button) {
        img.clearAnimation()
    }

    fun setAutoPlay(status: Boolean) {
        if (status != this.getAutoPlay()) {
            if (status) {
                //this.showMessage(getString(R.string.toast_autoplay_clip_on))
                //EXM05
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_autoplay_clip_on)
                    )
                }
            } else {
                //this.showMessage(getString(R.string.toast_autoplay_clip_off))
                //EXM06
                if (!isAbortConfirmation) {
                    showMessageDialog(
                        "",
                        getString(R.string.toast_autoplay_clip_off)
                    )
                }
            }
            getSharedPreferences(settingsSwitchData["AUTO_PLAY_CLIPS"], PRIVATE_MODE).edit()
                .putBoolean(settingsSwitchData["AUTO_PLAY_CLIPS"], status).apply()
        }
    }

    fun getAutoPlay(): Boolean {
        return getSharedPreferences(
            settingsSwitchData["AUTO_PLAY_CLIPS"],
            PRIVATE_MODE
        ).getBoolean(
            settingsSwitchData["AUTO_PLAY_CLIPS"],
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
        //println("todayDate: " + todayDate + " savedDate: " + savedDate)
        if (todayDate == savedDate) {
            return savedDate
        } else {
            return todayDate
        }
    }

    fun getContributing(type: String): String {
        //just if the user is logged-in
        if (this.logged) {
            when (type) {
                "validations" -> {
                    return statsPrefManager.todayValidated.toString()
                }
                "recordings" -> {
                    return statsPrefManager.todayRecorded.toString()
                }
                else -> {
                    return "?"
                }
            }
        } else {
            //user no logged
        }
        return "?"
    }

}
