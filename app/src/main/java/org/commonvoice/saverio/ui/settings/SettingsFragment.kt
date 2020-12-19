package org.commonvoice.saverio.ui.settings

import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.android.synthetic.main.fragment_settings.*
import org.commonvoice.saverio.*
import org.commonvoice.saverio.databinding.FragmentSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.commonvoice.saverio_lib.viewmodels.DashboardViewModel
import org.commonvoice.saverio_lib.viewmodels.MainActivityViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingsFragment : ViewBoundFragment<FragmentSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager: MainPrefManager by inject()
    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val speakPrefManager: SpeakPrefManager by inject()
    private val listenPrefManager: ListenPrefManager by inject()
    private val workManager by inject<WorkManager>()

    private val mainViewModel by viewModel<MainActivityViewModel>()
    private val dashboardViewModel by sharedViewModel<DashboardViewModel>()

    private val languagesListShort by lazy {
        resources.getStringArray(R.array.languages_short)
    }

    override fun onStart() = withBinding {
        super.onStart()

        setupLanguageSpinner()

        text_settingsTheme.setText(R.string.settingsLanguage)

        textRelease.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        textDevelopedBy.setText(R.string.txt_developed_by)

        setupSwitches()
        //setupButtons()
        additionalGPSSettings()

        setTheme()
    }

    private fun setupSwitches() {
        /*
        switchAnonymousStatistics.setOnCheckedChangeListener { _, isChecked ->
            mainPrefManager.areGeneralStats = isChecked
            mainViewModel.postStats(
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                MainActivity.SOURCE_STORE
            )
        }
        switchAnonymousStatistics.isChecked = mainPrefManager.areGeneralStats

        switchAutoPlayClips.setOnCheckedChangeListener { _, isChecked ->
            listenPrefManager.isAutoPlayClipEnabled = isChecked
        }
        switchAutoPlayClips.isChecked = listenPrefManager.isAutoPlayClipEnabled

        switchDarkTheme.isChecked = theme.isDark
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            theme.isDark = isChecked
            setTheme()
        }

        switchRecordingSound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                showMessageDialog(
                    "",
                    getString(R.string.toast_recording_indicator_sound_on)
                )

            speakPrefManager.playRecordingSoundIndicator = isChecked
        }
        switchRecordingSound.isChecked = speakPrefManager.playRecordingSoundIndicator

        switchCheckForUpdates.setOnCheckedChangeListener { _, isChecked ->
            settingsPrefManager.automaticallyCheckForUpdates = isChecked
        }
        switchCheckForUpdates.isChecked = settingsPrefManager.automaticallyCheckForUpdates

        switchGestures.setOnCheckedChangeListener { _, isChecked ->
            if (settingsPrefManager.showConfirmationMessages) {
                showMessageDialog(
                    "",
                    if (isChecked) getString(R.string.toast_gestures_on)
                    else getString(R.string.toast_gestures_off)
                )
            }

            mainPrefManager.areGesturesEnabled = isChecked
        }
        switchGestures.isChecked = mainPrefManager.areGesturesEnabled

        switchSkipRecordingsConfirmation.setOnCheckedChangeListener { _, isChecked ->
            if (settingsPrefManager.showConfirmationMessages) {
                showMessageDialog(
                    "",
                    if (isChecked) getString(R.string.toast_skip_recording_confirmation_on)
                    else getString(R.string.toast_skip_recording_confirmation_off)
                )
            }

            speakPrefManager.skipRecordingConfirmation = isChecked
        }
        switchSkipRecordingsConfirmation.isChecked = speakPrefManager.skipRecordingConfirmation


        switchExperimentalFeatures.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                //reset animations at true
                mainPrefManager.areAnimationsEnabled = true
                switchEnableAnimations.isChecked = true

                //reset show labels at false
                val navView: BottomNavigationView? = activity?.findViewById(R.id.nav_view)
                navView?.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
                mainPrefManager.areLabelsBelowMenuIcons = false
                switchShowLabelsBelowMenuIcons.isChecked = false
            }

            if (settingsPrefManager.showConfirmationMessages) {
                showMessageDialog(
                    "",
                    if (isChecked) getString(R.string.toast_experimental_features_on)
                    else getString(R.string.toast_experimental_featured_off)
                )
            }

            settingsSectionExperimentalFeatures.isGone = !isChecked
        }
        switchExperimentalFeatures.isChecked = settingsPrefManager.enableExperimentalFeatures
        settingsSectionExperimentalFeatures.isVisible =
            settingsPrefManager.enableExperimentalFeatures

        switchEnableAnimations.setOnCheckedChangeListener { _, isChecked ->
            mainPrefManager.areAnimationsEnabled = isChecked
        }
        switchEnableAnimations.isChecked = mainPrefManager.areAnimationsEnabled

        switchShowLabelsBelowMenuIcons.setOnCheckedChangeListener { _, isChecked ->
            val navView: BottomNavigationView? = activity?.findViewById(R.id.nav_view)
            if (isChecked) {
                navView?.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
            } else {
                navView?.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
            }
            mainPrefManager.areLabelsBelowMenuIcons = isChecked
        }
        switchShowLabelsBelowMenuIcons.isChecked = mainPrefManager.areLabelsBelowMenuIcons
    }

    private fun setupButtons() {
        buttonProjectGitHub.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/2PeOGRg")))
        }

        buttonContactOnTelegram.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/359wgbg")))
        }

        buttonOpenTutorial.onClick {
            Intent(requireContext(), FirstLaunch::class.java).also {
                startActivity(it)
                activity?.finish()
            }
        }

        buttonTranslateTheApp.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3bNBoUU")))
        }

        buttonSeeStatistics.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/35d2dza")))
        }

        buttonBuyMeACoffee.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3aJnnq7")))
        }

        buttonTelegramGroup.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3clgfkg")))
        }

        buttonReadGuidelines.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mzl.la/2Z5OxEQ")))
        }

        buttonReadCommonVoiceToS.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mzl.la/3b0dN3R")))
        }
        */
    }

    private fun additionalGPSSettings() {
        if (MainActivity.SOURCE_STORE == "GPS") {
            buttonBuyMeACoffee.isGone = true
            separator17.isGone = true
        }

        buttonReviewOnGooglePlay.onClick {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=org.commonvoice.saverio")
                )
            )
        }

        if (MainActivity.SOURCE_STORE == "GPS") {
            buttonReviewOnGooglePlay.isGone = false
            separator10.isGone = false
        }
    }

    private fun setupLanguageSpinner() {
        languageList.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.languages)
        )

        languageList.setSelection(languagesListShort.indexOf(mainPrefManager.language))

        languageList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLanguage = languagesListShort[position]

                if (selectedLanguage != mainPrefManager.language) {
                    mainPrefManager.language = selectedLanguage

                    mainViewModel.clearDB().invokeOnCompletion {
                        SentencesDownloadWorker.attachOneTimeJobToWorkManager(
                            workManager,
                            ExistingWorkPolicy.REPLACE
                        )
                        ClipsDownloadWorker.attachOneTimeJobToWorkManager(
                            workManager,
                            ExistingWorkPolicy.REPLACE
                        )

                        mainPrefManager.hasLanguageChanged = true

                        (activity as? MainActivity)?.setLanguageUI("restart")
                    }
                    dashboardViewModel.lastStatsUpdate = 0
                }
            }
        }
    }

    private fun setTheme() = withBinding {
        /*
        theme.setElements(requireContext(), layoutSettings)

        theme.setElements(requireContext(), settingsSectionLanguage)
        theme.setElements(requireContext(), settingsSectionListen)
        theme.setElements(requireContext(), settingsSectionSpeak)
        theme.setElements(requireContext(), settingsSectionOther)
        theme.setElements(requireContext(), settingsSectionExperimentalFeatures)
        theme.setElements(requireContext(), settingsSectionBottom)

        theme.setElement(requireContext(), 3, settingsSectionLanguage)
        theme.setElement(requireContext(), 3, settingsSectionListen)
        theme.setElement(requireContext(), 3, settingsSectionSpeak)
        theme.setElement(requireContext(), 3, settingsSectionOther)
        theme.setElement(requireContext(), 3, settingsSectionExperimentalFeatures)
        theme.setElement(requireContext(), 1, settingsSectionBottom)


        theme.setElement(requireContext(), textRelease, background = false)

        theme.setElement(requireContext(), buttonContactOnTelegram, background = false)
        theme.setElement(requireContext(), buttonBuyMeACoffee, background = false)
        theme.setElement(requireContext(), buttonProjectGitHub, background = false)
        theme.setElement(requireContext(), buttonTranslateTheApp, background = false)
        theme.setElement(requireContext(), buttonOpenTutorial, background = false)
        theme.setElement(requireContext(), buttonSeeStatistics, background = false)
        theme.setElement(requireContext(), buttonCustomiseTheFontSize, background = false)
        theme.setElement(requireContext(), buttonCustomiseGestures, background = false)
        theme.setElement(requireContext(), buttonThemes, background = false)
        theme.setElement(requireContext(), buttonTelegramGroup, background = false)
        theme.setElement(requireContext(), buttonReadGuidelines, background = false)
        theme.setElement(requireContext(), buttonReadCommonVoiceToS, background = false)
        theme.setElement(requireContext(), buttonReviewOnGooglePlay, background = false)

        theme.setElement(imageLanguageIcon, R.drawable.ic_language, R.drawable.ic_language)
        */
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