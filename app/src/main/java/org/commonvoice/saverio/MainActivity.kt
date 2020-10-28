package org.commonvoice.saverio

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.TextView
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
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.utils.TranslationLanguages
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.RecordingsUploadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.HashMap


class MainActivity : VariableLanguageActivity(R.layout.activity_main) {

    private val workManager: WorkManager by inject()
    private val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val firstRunPrefManager: FirstRunPrefManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()

    companion object {
        const val SOURCE_STORE = BuildConfig.FLAVOR
        const val RECORD_REQUEST_CODE = 8374
    }

    private val urlWithoutLang: String = "https://commonvoice.mozilla.org/api/v1/" //API url (without lang)

    private val languagesListShortArray by lazy {
        resources.getStringArray(R.array.languages_short)
    }
    private val languagesListArray by lazy {
        resources.getStringArray(R.array.languages)
    }

    var logged: Boolean = false
    var userId: String = ""

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
            Intent(this, FirstLaunch::class.java).also {
                startActivity(it)
                finish()
            }
        } else {
            setLanguageUI("start")
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

        this.checkIfSessionIsExpired()
        this.reviewOnPlayStore()

        if (mainPrefManager.showReportWebsiteBugs) {
            showMessageDialog("", getString(R.string.text_report_website_bug), type = 11)
        }
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

    private fun reviewOnPlayStore() {
        //just if it's the GPS version
        if (SOURCE_STORE == "GPS") {
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

    fun refreshDailyGoalDataInDashboard() {
        //refresh data of Daily goal in Dashboard
        val goalText = this.findViewById<TextView>(R.id.labelDashboardDailyGoalValue)
        if (statsPrefManager.dailyGoalObjective == 0) {
            goalText.text = getString(R.string.daily_goal_is_not_set)
            goalText.typeface = Typeface.DEFAULT
            this.findViewById<TextView>(R.id.buttonDashboardSetDailyGoal).text =
                getString(R.string.set_daily_goal)
            //println("Daily goal is not set")
        } else {
            goalText.text = statsPrefManager.dailyGoalObjective.toString()
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
            message.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
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
                    Intent(this, SpeakActivity::class.java).also {
                        startActivity(it)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 111 || requestCode == 110) && resultCode == RESULT_OK) {
            dashboardViewModel.updateStats(force = true)
            recreate()
        } else {
            recreate()
        }
    }

    fun setLanguageUI(type: String) {
        val restart: Boolean = mainPrefManager.hasLanguageChanged
        val restart2: Boolean = mainPrefManager.hasLanguageChanged2

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
                if (TranslationLanguages.isUncompleted(mainPrefManager.language)) {
                    detailsMessage =
                        "\n" + getString(R.string.message_app_not_completely_translated)
                }
                showMessageDialog(
                    "",
                    getString(R.string.toast_language_changed).replace(
                        "{{*{{lang}}*}}",
                        languagesListArray[languagesListShortArray.indexOf(mainPrefManager.language)]
                    ) + detailsMessage
                )
            }
        }
    }

}
