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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.utils.TranslationLanguages
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.RecordingsUploadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
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
    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val firstRunPrefManager: FirstRunPrefManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()
    private val settingsPrefManager by inject<SettingsPrefManager>()

    companion object {
        const val SOURCE_STORE = BuildConfig.FLAVOR
    }

    private val RECORD_REQUEST_CODE = 101
    private val PRIVATE_MODE = 0
    //"LOGGED" //false->no logged-in || true -> logged-in
    //"LAST_STATS_EVERYONE" //yyyy/mm/dd hh:mm:ss
    //"LAST_STATS_YOU" //yyyy/mm/dd hh:mm:ss
    //"TODAY_CONTRIBUTING" //saved as "yyyy/mm/dd, n_recorded, n_validated"

    private val urlWithoutLang: String = "https://commonvoice.mozilla.org/api/v1/" //API url (without lang)

    private val languagesListShortArray by lazy {
        resources.getStringArray(R.array.languages_short)
    }
    private val languagesListArray by lazy {
        resources.getStringArray(R.array.languages)
    }

    private val selectedLanguageVar get() = mainPrefManager.language
    var logged: Boolean = false
    var userId: String = ""
    var userName: String = ""

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

        if (mainPrefManager.areLabelsBelowMenuIcons) {
            navView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
        } else {
            navView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
        }

        if (firstRunPrefManager.main) {
            // close main and open tutorial -- first run
            this.openTutorial()
        } else {
            this.setLanguageUI("start")
            //checkPermissions()

            RecordingsUploadWorker.attachToWorkManager(workManager)
            SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
            ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)

            mainActivityViewModel.postStats(
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                SOURCE_STORE
            )
        }

        this.checkUserLoggedIn()

        this.checkIfSessionIsExpired()
        this.reviewOnPlayStore()

        this.checkReportWebsiteBugs()

        this
    }

    fun checkReportWebsiteBugs() {
        if (mainPrefManager.showReportWebsiteBugs) {
            showMessageDialog("", getString(R.string.text_report_website_bug), type = 11)
        }
    }

    fun setReportWebsiteBugs(value: Boolean = true) {
        mainPrefManager.showReportWebsiteBugs = value
    }

    fun reportBugOnMozillaDiscourse() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mzl.la/3f7sHqj")))
    }

    fun reportBugOnGitHubRepository() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/2Z73TZZ")))
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
        mainPrefManager.isLoggedIn = false
        mainPrefManager.username = ""

        statsPrefManager.allTimeLevel = 0
        statsPrefManager.allTimeRecorded = 0
        statsPrefManager.allTimeValidated = 0

        statsPrefManager.dailyGoalObjective = 0

        mainActivityViewModel.clearDB()
    }

    fun setLevelRecordingsValidations(type: Int, value: Int) {
        when (type) {
            0 -> {
                //level
                statsPrefManager.allTimeLevel = value
            }
            1 -> {
                //recordings
                statsPrefManager.allTimeRecorded = value
            }
            2 -> {
                //validations
                statsPrefManager.allTimeValidated = value
            }
        }
    }

    fun reviewOnPlayStore() {
        //just if it's the GPS version
        if (getSourceStore() == "GPS") {
            val counter = statsPrefManager.reviewOnPlayStoreCounter
            val times = 100 //after this times it will show the message
            if (((counter % times) == 0 || (counter % times) == times)) {
                showMessageDialog(
                    "",
                    getString(R.string.message_review_app_on_play_store)
                )
            }
            statsPrefManager.reviewOnPlayStoreCounter++
        }
    }

    fun getSourceStore(): String {
        return MainActivity.SOURCE_STORE
    }

    fun checkNewVersionAvailable(forcedCheck: Boolean = false) {
        if (settingsPrefManager.automaticallyCheckForUpdates or forcedCheck) {
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
        this.logged = mainPrefManager.isLoggedIn

        if (logged) {
            this.userId = mainPrefManager.sessIdCookie!!
            this.userName = mainPrefManager.username
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
        this.logged = mainPrefManager.isLoggedIn

        if (logged) {
            this.userId = mainPrefManager.sessIdCookie ?: ""

            this.userName = mainPrefManager.username
        }
    }

    fun getDailyGoal(): Int {
        return statsPrefManager.dailyGoalObjective
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
            var message: MessageDialog? = null
            message = MessageDialog(
                this,
                type,
                title,
                messageText,
                details = details,
                height = height
            )
            if (type == 11) message.setMainActivity(this)
            message?.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
    }

    fun openTutorial() {
        Intent(this, FirstLaunch::class.java).also {
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_REQUEST_CODE
                )
            } else {
                openActualSpeakSection()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openActualSpeakSection()
                }
            }
        }
    }

    private fun openActualSpeakSection() {
        Intent(this, SpeakActivity::class.java).also {
            startActivity(it)
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
            dashboardViewModel.updateStats(force = true)
            recreate()
        } else {
            //println("MainActivity updated (2)")
            recreate()
        }
    }

    fun noLoggedInNoDailyGoal() {
        //EXM20
        showMessageDialog(
            "",
            getString(R.string.toastNoLoginNoDailyGoal)
        )
    }

    fun setLanguageUI(type: String) {
        val restart: Boolean = mainPrefManager.hasLanguageChanged
        val restart2: Boolean = mainPrefManager.hasLanguageChanged2

        //println("-->sel: " + selectedLanguageVar + " -->lang: " + getString(R.string.language))
        //println("-->index: " + translations_languages.indexOf(lang))
        val android6 = Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
        if (android6) {
            //Android 6.0
            val tempLang = getSharedPreferences("LANGUAGE", 0).getString("LANGUAGE", "en")
            var lang = tempLang!!.split("-")[0]
            if (!TranslationLanguages.isSupported(lang)) {
                lang = TranslationLanguages.defaultLanguage
            }
            val locale: Locale = Locale(lang)
            Locale.setDefault(locale)
            val res: Resources = resources
            val config: Configuration = res.configuration
            config.setLocale(locale)
            res.updateConfiguration(config, res.displayMetrics)
        }
        if (restart || type == "restart") {
            mainPrefManager.hasLanguageChanged = true

            if (android6) {
                mainPrefManager.hasLanguageChanged = false

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
                mainPrefManager.hasLanguageChanged2 = false

                var detailsMessage = ""
                if (TranslationLanguages.isUncompleted(this.getSelectedLanguage())) {
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
        }
    }

}
