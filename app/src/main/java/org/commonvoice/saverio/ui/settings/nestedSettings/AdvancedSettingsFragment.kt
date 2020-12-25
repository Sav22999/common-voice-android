package org.commonvoice.saverio.ui.settings.nestedSettings

import android.content.Intent
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.CookieManager
import kotlinx.android.synthetic.main.fragment_advanced_settings.*
import org.commonvoice.saverio.FirstLaunch
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.MessageDialog
import org.commonvoice.saverio.databinding.FragmentAdvancedSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio_lib.preferences.*
import org.commonvoice.saverio_lib.viewmodels.LoginViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdvancedSettingsFragment : ViewBoundFragment<FragmentAdvancedSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAdvancedSettingsBinding {
        return FragmentAdvancedSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()
    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val statsPrefManager by inject<StatsPrefManager>()
    private val listenPrefManager by inject<ListenPrefManager>()
    private val speakPrefManager by inject<SpeakPrefManager>()
    private val firstRunPrefManager by inject<FirstRunPrefManager>()
    private val logPrefManager by inject<LogPrefManager>()
    private val loginViewModel by viewModel<LoginViewModel>()

    override fun onStart() {
        super.onStart()

        buttonBackSettingsSubSectionAdvanced.setOnClickListener {
            activity?.onBackPressed()
        }

        withBinding {
            switchGenericStatistics.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areGenericStats = isChecked
            }
            switchGenericStatistics.isChecked = mainPrefManager.areGenericStats

            switchAppUsageStatistics.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.areAppUsageStatsEnabled = isChecked
            }
            switchAppUsageStatistics.isChecked = mainPrefManager.areAppUsageStatsEnabled

            switchSaveLogToFile.setOnCheckedChangeListener { _, isChecked ->
                logPrefManager.saveLogFile = isChecked
            }
            switchSaveLogToFile.isChecked = logPrefManager.saveLogFile

            buttonOpenTutorialAgain.setOnClickListener {
                Intent(requireContext(), FirstLaunch::class.java).also {
                    startActivity(it)
                    activity?.finish()
                }
            }

            buttonResetData.setOnClickListener {
                //TODO: reset data (reset all settings to default value and logout)

                //Reset FirstRun
                firstRunPrefManager.main = true
                firstRunPrefManager.listen = true
                firstRunPrefManager.speak = true

                //Reset Settings
                settingsPrefManager.isOfflineMode = true
                settingsPrefManager.showReportIcon = false
                settingsPrefManager.automaticallyCheckForUpdates = true
                settingsPrefManager.latestVersion = ""

                //Reset Stats
                statsPrefManager.dailyGoalObjective = 0
                statsPrefManager.reviewOnPlayStoreCounter = 0
                statsPrefManager.todayValidated = 0
                statsPrefManager.todayRecorded = 0
                statsPrefManager.allTimeValidated = 0
                statsPrefManager.allTimeRecorded = 0
                statsPrefManager.allTimeLevel = 0

                //Reset Listen
                listenPrefManager.requiredClipsCount = 50
                listenPrefManager.isAutoPlayClipEnabled = false
                listenPrefManager.isShowTheSentenceAtTheEnd = false

                //Reset Speak
                speakPrefManager.requiredSentencesCount = 50
                speakPrefManager.playRecordingSoundIndicator = false
                speakPrefManager.skipRecordingConfirmation = false
                speakPrefManager.saveRecordingsOnDevice = false

                //Reset Main
                mainPrefManager.language = "en"
                mainPrefManager.tokenUserId = ""
                mainPrefManager.tokenAuth = ""
                mainPrefManager.showOfflineModeMessage = true
                mainPrefManager.showReportWebsiteBugs = true
                mainPrefManager.areGesturesEnabled = true
                mainPrefManager.statsUserId = ""
                mainPrefManager.areGenericStats = true
                mainPrefManager.areAppUsageStatsEnabled = true
                mainPrefManager.areAnimationsEnabled = true
                mainPrefManager.areLabelsBelowMenuIcons = false
                mainPrefManager.hasLanguageChanged = true
                mainPrefManager.hasLanguageChanged2 = true
                mainPrefManager.themeType = "light"
                mainPrefManager.sessIdCookie = null
                mainPrefManager.isLoggedIn = false
                mainPrefManager.username = ""

                //Reset Log
                logPrefManager.saveLogFile = false

                CookieManager.getInstance().flush()
                CookieManager.getInstance().removeAllCookies(null)
                loginViewModel.clearDB()

                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
        }

        setupButtons()

        setTheme()
    }

    fun setupButtons() {
        buttonShowStringIdentifyMe.setOnClickListener {
            showMessageDialog("", mainPrefManager.statsUserId)
        }
    }

    fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsAdvanced)

            theme.setElements(requireContext(), settingsSectionAdvanced)

            theme.setElement(requireContext(), 3, settingsSectionAdvanced)
        }
    }

    private fun showMessageDialog(
        title: String,
        text: String,
        errorCode: String = "",
        details: String = "",
        type: Int = 0
    ) {
        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
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
            val message = MessageDialog(
                requireContext(),
                type,
                title,
                messageText,
                details = details,
                height = height
            )
            message.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
    }
}