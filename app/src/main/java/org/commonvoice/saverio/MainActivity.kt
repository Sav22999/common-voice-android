package org.commonvoice.saverio

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.coroutines.launch
import org.commonvoice.saverio.ui.VariableLanguageActivity
import org.commonvoice.saverio.ui.dialogs.DialogInflater
import org.commonvoice.saverio.ui.dialogs.commonTypes.CheckboxedStandardDialog
import org.commonvoice.saverio.ui.dialogs.commonTypes.StandardDialog
import org.commonvoice.saverio.ui.dialogs.specificDialogs.ReportBugsDialog
import org.commonvoice.saverio.utils.NotificationsDailyGoalReceiver
import org.commonvoice.saverio.utils.TranslationHandler
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.RecordingsUploadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class MainActivity : VariableLanguageActivity(R.layout.activity_main) {

    private val workManager: WorkManager by inject()
    private val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val firstRunPrefManager: FirstRunPrefManager by inject()
    private val statsPrefManager: StatsPrefManager by inject()

    private val speakPrefManager: SpeakPrefManager by inject()
    private val listenPrefManager: ListenPrefManager by inject()

    private val connectionManager: ConnectionManager by inject()
    private val translationHandler: TranslationHandler by inject()

    private val dialogInflater by inject<DialogInflater>()
    private val mainViewModel by viewModel<MainActivityViewModel>()
    private lateinit var viewModel: GenericViewModel

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

        checkToday()

        checkIfSessionIsExpired()
        reviewOnPlayStore()
        showBuyMeACoffeeDialog()
        checkAdsDisabledGPSVersion()

        checkNotification(0, 0, 1)

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

    @SuppressLint("SimpleDateFormat")
    private fun checkToday() {
        var daysInARow = statsPrefManager.daysInARow
        var lastDateOpenedTheApp = statsPrefManager.lastDateOpenedTheApp


        val today = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else {
            SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()
        }
        val yesterday = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate.now().minusDays(1).toString()
        } else {
            SimpleDateFormat("yyyy-MM-dd").format(yesterday()).toString()
        }
        if (lastDateOpenedTheApp == null || lastDateOpenedTheApp == yesterday) {
            statsPrefManager.lastDateOpenedTheApp = today
            if (lastDateOpenedTheApp == null) statsPrefManager.daysInARow = 1
            else statsPrefManager.daysInARow = daysInARow + 1
            statsPrefManager.daysInARowShown = false
        }
    }

    private fun yesterday(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return cal.time
    }

    private fun showBuyMeACoffeeDialog() {
        val counter = statsPrefManager.buyMeACoffeeCounter
        val times = 200 //after this times it will show the message
        if ((((counter % times) == 0 || (counter % times) == times)) && mainPrefManager.showDonationDialog && counter > 0) {
            dialogInflater.show(this, CheckboxedStandardDialog(
                messageRes = R.string.text_buy_me_a_coffee,
                buttonTextRes = R.string.liberapay_name,
                button2TextRes = R.string.paypal_name,
                onButtonClick = {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.liberapay.com/Sav22999")
                        )
                    )
                }, onButton2Click = {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.paypal.me/saveriomorelli")
                        )
                    )
                }, overrideItalicStyle = true,
                initialCheckboxState = !mainPrefManager.showDonationDialog,
                onCheckBoxStateChange = { state ->
                    mainPrefManager.showDonationDialog = !state
                }
            )
            )
        }
        statsPrefManager.buyMeACoffeeCounter++
    }

    private fun logoutUser() {
        dialogInflater.show(this,
            StandardDialog(
                messageRes = R.string.message_log_in_again,
                button2TextRes = R.string.text_log_in_again,
                onButton2Click = {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            ))

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
        if (SOURCE_STORE == "GPS" && !BuildConfig.DEBUG) {
            val counter = statsPrefManager.reviewOnPlayStoreCounter
            val times = 100 //after this times it will show the message
            if ((((counter % times) == 0 || (counter % times) == 1 || (counter % times) == 2 || (counter % times) == times)) && mainPrefManager.showReviewAppDialog && counter > 3) {
                dialogInflater.show(
                    this, CheckboxedStandardDialog(
                        messageRes = R.string.message_review_app_on_play_store,
                        buttonTextRes = R.string.text_review_now,
                        onButtonClick = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=org.commonvoice.saverio")
                                )
                            )
                        }, overrideItalicStyle = true,
                        initialCheckboxState = !mainPrefManager.showReviewAppDialog,
                        onCheckBoxStateChange = { state ->
                            mainPrefManager.showReviewAppDialog = !state
                        }
                    )
                )
            }
            statsPrefManager.reviewOnPlayStoreCounter++
        }
    }

    private fun checkAdsDisabledGPSVersion() {
        //just if it's the GPS version
        if (SOURCE_STORE == "GPS" && (!mainPrefManager.showAdBanner || !speakPrefManager.showAdBanner || !listenPrefManager.showAdBanner) && mainPrefManager.showEnableAdsDialog) {
            val counter = statsPrefManager.checkAdsDisabledGPS
            val times = 50 //after this times it will show the message
            if (((counter % times) == 0 || (counter % times) == times)) {
                dialogInflater.show(
                    this, CheckboxedStandardDialog(
                        messageRes = R.string.text_ads_are_disable_would_you_like_able,
                        buttonTextRes = R.string.text_open_settings_now,
                        onButtonClick = { //TODO move this to HomeFragment
                            try {
                                findNavController(R.id.nav_host_fragment).navigate(R.id.advancedSettingsFragment)
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }, overrideItalicStyle = true,
                        initialCheckboxState = !mainPrefManager.showEnableAdsDialog,
                        onCheckBoxStateChange = { state ->
                            mainPrefManager.showEnableAdsDialog = !state
                        }
                    )
                )
            }
            statsPrefManager.checkAdsDisabledGPS++
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

                if (!translationHandler.isLanguageComplete(mainPrefManager.language)) {
                    val detailsMessage =
                        "\n" + getString(R.string.message_app_not_completely_translated)
                    dialogInflater.show(
                        this, StandardDialog(
                            message = getString(R.string.toast_language_changed).replace(
                                "{{lang}}",
                                translationHandler.getLanguageName(mainPrefManager.language)
                            ) + detailsMessage,
                            button2TextRes = R.string.button_translate_on_crowdin,
                            onButton2Click = {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://crowdin.com/project/common-voice-android")
                                    )
                                )
                            }
                        )
                    )
                } else {
                    dialogInflater.show(
                        this, StandardDialog(
                            message = getString(R.string.toast_language_changed).replace(
                                "{{lang}}",
                                translationHandler.getLanguageName(mainPrefManager.language)
                            )
                        )
                    )
                }
                resetData()
            }
        }
    }

    fun checkNotification(hour: Int, minute: Int, second: Int) {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, second)

            val notificationIntent = Intent(this, NotificationsDailyGoalReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                100,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                10000,
                pendingIntent
            )
        } catch (e: Exception) {
            //Exception
            Log.e("Error", e.toString())
        }
    }

    private fun resetData() {
        statsPrefManager.todayValidated = 0
        statsPrefManager.todayRecorded = 0
        statsPrefManager.localValidated = 0
        statsPrefManager.localRecorded = 0
        statsPrefManager.localLevel = 0
    }
}
