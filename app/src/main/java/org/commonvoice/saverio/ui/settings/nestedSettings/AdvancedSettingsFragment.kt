package org.commonvoice.saverio.ui.settings.nestedSettings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.fragment_advanced_settings.*
import kotlinx.android.synthetic.main.fragment_ui_settings.*
import org.commonvoice.saverio.FirstLaunch
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.MessageDialog
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentAdvancedSettingsBinding
import org.commonvoice.saverio.ui.dialogs.DialogInflater
import org.commonvoice.saverio.ui.dialogs.specificDialogs.IdentifyMeDialog
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.setupOnSwipeRight
import org.commonvoice.saverio_lib.preferences.*
import org.commonvoice.saverio_lib.repositories.StatsRepository
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
    private val dialogInflater by inject<DialogInflater>()
    private val statsRepository by inject<StatsRepository>()

    override fun onStart() {
        super.onStart()

        buttonBackSettingsSubSectionAdvanced.setOnClickListener {
            activity?.onBackPressed()
        }

        withBinding {
            if (mainPrefManager.areGesturesEnabled)
                nestedScrollSettingsAdvanced.setupOnSwipeRight(requireContext()) { activity?.onBackPressed() }

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

            switchHomeAds.text = getString(R.string.enable_ads_google_play_in_section).replace(
                "{{*{{section_name}}*}}",
                getString(R.string.title_home)
            )
            switchHomeAds.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.showAdBanner = isChecked
            }
            switchHomeAds.isChecked = mainPrefManager.showAdBanner

            switchListenAds.text = getString(R.string.enable_ads_google_play_in_section).replace(
                "{{*{{section_name}}*}}",
                getString(R.string.settingsListen)
            )
            switchListenAds.setOnCheckedChangeListener { _, isChecked ->
                listenPrefManager.showAdBanner = isChecked
            }
            switchListenAds.isChecked = listenPrefManager.showAdBanner

            switchSpeakAds.text = getString(R.string.enable_ads_google_play_in_section).replace(
                "{{*{{section_name}}*}}",
                getString(R.string.settingsSpeak)
            )
            switchSpeakAds.setOnCheckedChangeListener { _, isChecked ->
                speakPrefManager.showAdBanner = isChecked
            }
            switchSpeakAds.isChecked = speakPrefManager.showAdBanner

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
                statsPrefManager.localValidated = 0
                statsPrefManager.localRecorded = 0
                statsPrefManager.localLevel = 0

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

    private fun setupButtons() {
        buttonShowStringIdentifyMe.setOnClickListener {
            dialogInflater.show(requireContext(),
                IdentifyMeDialog(statsRepository.getUserId(), onCopyClick = {
                    requireContext()
                        .getSystemService<ClipboardManager>()
                        ?.setPrimaryClip(ClipData.newPlainText("", statsRepository.getUserId()))
                }))
        }
    }

    private fun setTheme() {
        withBinding {
            theme.setElement(layoutSettingsAdvanced)

            theme.setElements(requireContext(), settingsSectionAdvanced)
            theme.setElements(requireContext(), settingsSectionAdvancedAds)

            theme.setElement(requireContext(), 3, settingsSectionAdvanced)
            theme.setElement(requireContext(), 3, settingsSectionAdvancedAds)

            theme.setTitleBar(requireContext(), titleSettingsSubSectionAdvanced, textSize = 20F)
        }
    }

}