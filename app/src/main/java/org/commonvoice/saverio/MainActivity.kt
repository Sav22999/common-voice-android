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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.ui.dialogs.DialogInflater
import org.commonvoice.saverio.ui.dialogs.commonTypes.StandardDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.ReportBugsDialog
import org.commonvoice.saverio.utils.TranslationHandler
import org.commonvoice.saverio_lib.api.network.ConnectionManager
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


class MainActivity : VariableLanguageActivity(R.layout.activity_main) {

    private val workManager: WorkManager by inject()
    private val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val firstRunPrefManager: FirstRunPrefManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()

    private val connectionManager: ConnectionManager by inject()
    private val translationHandler: TranslationHandler by inject()

    private val dialogInflater by inject<DialogInflater>()

    companion object {
        val SOURCE_STORE: String = BuildConfig.SOURCE_STORE
        const val RECORD_REQUEST_CODE = 8374
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        if (mainPrefManager.areLabelsBelowMenuIcons) {
            navView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
        } else {
            navView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
        }

        //Needed for the App Usage worker
        mainPrefManager.appVersionCode = BuildConfig.VERSION_CODE
        mainPrefManager.appSourceStore = SOURCE_STORE

        mainPrefManager.isAlpha = BuildConfig.VERSION_NAME.contains("a")
        mainPrefManager.isBeta = BuildConfig.VERSION_NAME.contains("b")

        lifecycleScope.launch {
            translationHandler.updateLanguages()
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

        checkIfSessionIsExpired()
        reviewOnPlayStore()

        if (mainPrefManager.showReportWebsiteBugs) {
            if (statsPrefManager.reviewOnPlayStoreCounter >= 5) {
                dialogInflater.show(this, ReportBugsDialog(this, mainPrefManager))
            }
        }
    }

    fun resetStatusBarColor() {
        this@MainActivity.window.statusBarColor =
            ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark)
    }

    private fun checkIfSessionIsExpired() {
        if (mainPrefManager.sessIdCookie != null && connectionManager.isInternetAvailable) {
            mainActivityViewModel.getUserClient().observe(this) {
                if (it == null) {
                    logoutUser()
                }
            }
        }
    }

    private fun logoutUser() {
        dialogInflater.show(this, StandardDialog(messageRes = R.string.message_log_in_again))

        mainPrefManager.sessIdCookie = null
        mainPrefManager.isLoggedIn = false
        mainPrefManager.username = ""

        statsPrefManager.allTimeLevel = 0
        statsPrefManager.allTimeRecorded = 0
        statsPrefManager.allTimeValidated = 0

        statsPrefManager.localValidated = 0
        statsPrefManager.localRecorded = 0
        statsPrefManager.localLevel = 0

        statsPrefManager.dailyGoalObjective = 0

        mainActivityViewModel.clearDB()
    }

    private fun reviewOnPlayStore() {
        //just if it's the GPS version
        if (SOURCE_STORE == "GPS") {
            val counter = statsPrefManager.reviewOnPlayStoreCounter
            val times = 100 //after this times it will show the message
            if (((counter % times) == 0 || (counter % times) == times)) {
                dialogInflater.show(this, StandardDialog(messageRes = R.string.message_review_app_on_play_store))
            }
            statsPrefManager.reviewOnPlayStoreCounter++
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
            if (!translationHandler.isLanguageSupported(lang)) {
                lang = TranslationHandler.DEFAULT_LANGUAGE
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
                if (!translationHandler.isLanguageComplete(mainPrefManager.language)) {
                    detailsMessage =
                        "\n" + getString(R.string.message_app_not_completely_translated)
                }
                dialogInflater.show(this, StandardDialog(message = getString(R.string.toast_language_changed).replace(
                    "{{*{{lang}}*}}",
                    translationHandler.getLanguageName(mainPrefManager.language)
                ) + detailsMessage))
                resetData()
            }
        }
    }

    private fun resetData() {
        statsPrefManager.todayValidated = 0
        statsPrefManager.todayRecorded = 0
        statsPrefManager.allTimeValidated = 0
        statsPrefManager.allTimeRecorded = 0
        statsPrefManager.allTimeLevel = 0
        statsPrefManager.localValidated = 0
        statsPrefManager.localRecorded = 0
        statsPrefManager.localLevel = 0
    }
}
