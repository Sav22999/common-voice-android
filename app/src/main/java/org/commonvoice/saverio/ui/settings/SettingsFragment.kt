package org.commonvoice.saverio.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import org.commonvoice.saverio.BuildConfig
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SpeakPrefManager
import org.koin.android.ext.android.inject


class SettingsFragment : Fragment() {

    private val mainPrefManager: MainPrefManager by inject()
    private val speakPrefManager: SpeakPrefManager by inject()
    private val listenPrefManager: ListenPrefManager by inject()

    var languagesListShort =
        arrayOf("en") // don't change it manually -> it will import automatically
    var languagesList =
        arrayOf("English") // don't change it manually -> it will import automatically
    var isAlpha: Boolean = false
    var theme: DarkLightTheme = DarkLightTheme()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        val main = (activity as MainActivity)
        main.dashboard_selected = false

        val textLanguage: TextView = root.findViewById(R.id.text_settingsLanguage)
        textLanguage.text = getText(R.string.settingsLanguage)
        //val model = ViewModelProviders.of(activity!!).get(SettingsViewModel::class.java)

        val releaseNumber: TextView = root.findViewById(R.id.textRelease)
        releaseNumber.text = (BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")")

        // import the languages list (short and "standard" from mainactivity)
        this.languagesListShort = main.languagesListShortArray
        this.languagesList = main.languagesListArray

        val language: Spinner = root.findViewById(R.id.languageList)
        language.adapter = main.getLanguageList()

        val selectedLanguage: String = main.getSelectedLanguage()

        language.setSelection(languagesListShort.indexOf(selectedLanguage))

        language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                main.setLanguageSettings(languagesListShort.get(position))
            }
        }

        val textProjectGithub: Button = root.findViewById(R.id.buttonProjectGitHub)
        textProjectGithub.setOnClickListener {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://bit.ly/2PeOGRg")
                )
            startActivity(browserIntent)
        }

        val btnContactDeveloperTelegram: Button = root.findViewById(R.id.buttonContactOnTelegram)
        btnContactDeveloperTelegram.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/359wgbg"))
            startActivity(browserIntent)
        }

        val btnOpenTutorial: Button = root.findViewById(R.id.buttonOpenTutorial)
        btnOpenTutorial.setOnClickListener {
            main.openTutorial()
        }

        val txtContributors: TextView = root.findViewById(R.id.textContributors)
        txtContributors.text = getString(R.string.txt_contributors)

        val txtDevelopedBy: TextView = root.findViewById(R.id.textDevelopedBy)
        txtDevelopedBy.text = getString(R.string.txt_developed_by)

        main.checkConnection()

        val switchAutoPlaySettings: Switch = root.findViewById(R.id.switchAutoPlayClips)
        switchAutoPlaySettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            listenPrefManager.isAutoPlayClipEnabled = isChecked
            main.setAutoPlay(isChecked)
        }
        switchAutoPlaySettings.isChecked = main.getAutoPlay()

        val switchDarkThemeSettings: Switch = root.findViewById(R.id.switchDarkTheme)
        switchDarkThemeSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            main.setDarkThemeSwitch(isChecked)
            setTheme(main, root)
        }
        switchDarkThemeSettings.isChecked = theme.getTheme(main)

        val switchStatisticsSettings: Switch = root.findViewById(R.id.switchAnonymousStatistics)
        switchStatisticsSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            main.setStatisticsSwitch(isChecked)
        }
        switchStatisticsSettings.isChecked = main.getStatisticsSwitch()

        val switchExperimentalFeaturesSettings: Switch =
            root.findViewById(R.id.switchExperimentalFeatures)
        switchExperimentalFeaturesSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            main.setExperimentalFeaturesSwitch(isChecked)
        }
        switchExperimentalFeaturesSettings.isChecked = main.getExperimentalFeaturesSwitch()

        val btnTranslateTheApp: Button = root.findViewById(R.id.buttonTranslateTheApp)
        btnTranslateTheApp.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3bNBoUU"))
            startActivity(browserIntent)
        }

        val btnSeeStatistics: Button = root.findViewById(R.id.buttonSeeStatistics)
        btnSeeStatistics.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/35d2dza"))
            startActivity(browserIntent)
        }

        val btnBuyMeACoffee: Button = root.findViewById(R.id.buttonBuyMeACoffee)
        btnBuyMeACoffee.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3aJnnq7"))
            startActivity(browserIntent)
        }

        if (main.getSourceStore() == "GPS") {
            btnBuyMeACoffee.isGone = true
            val separator17: View = root.findViewById(R.id.separator17)
            separator17.isGone = true
        }

        val recordingIndicatorSoundSettings: Switch = root.findViewById(R.id.switchRecordingSound)
        recordingIndicatorSoundSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            speakPrefManager.playRecordingSoundIndicator = isChecked
            main.setRecordingIndicatorSoundSwitch(isChecked)
        }
        recordingIndicatorSoundSettings.isChecked = main.getRecordingIndicatorSoundSwitch()

        val checkForUpdatesSettings: Switch = root.findViewById(R.id.switchCheckForUpdates)
        checkForUpdatesSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            main.setCheckForUpdatesSwitch(isChecked)
        }
        checkForUpdatesSettings.isChecked = main.getCheckForUpdatesSwitch()

        val abortConfirmationDialogsSettings: Switch =
            root.findViewById(R.id.switchAbortConfirmationDialogsInSettings)
        abortConfirmationDialogsSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            main.setAbortConfirmationDialogsInSettingsSwitch(isChecked)
        }
        abortConfirmationDialogsSettings.isChecked =
            main.getAbortConfirmationDialogsInSettingsSwitch()
        main.isAbortConfirmation = abortConfirmationDialogsSettings.isChecked

        val gesturesSettings: Switch = root.findViewById(R.id.switchGestures)
        gesturesSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            mainPrefManager.areGesturesEnabled = isChecked
            main.setGesturesSettingsSwitch(isChecked)
        }
        gesturesSettings.isChecked = main.getGesturesSettingsSwitch()

        val skipRecordingsConfirmationSettings: Switch =
            root.findViewById(R.id.switchSkipRecordingsConfirmation)
        skipRecordingsConfirmationSettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            speakPrefManager.skipRecordingConfirmation = isChecked
            main.setSkipRecordingsConfirmationSwitch(isChecked)
        }
        skipRecordingsConfirmationSettings.isChecked = main.getSkipRecordingsConfirmationSwitch()

        setTheme(main, root)

        return root
    }

    fun setTheme(view: Context, root: View) {
        val isDark = theme.getTheme(view)
        theme.setElements(view, root.findViewById(R.id.layoutSettings))

        theme.setElements(view, root.findViewById(R.id.settingsSectionLanguage))
        theme.setElements(view, root.findViewById(R.id.settingsSectionListen))
        theme.setElements(view, root.findViewById(R.id.settingsSectionSpeak))
        theme.setElements(view, root.findViewById(R.id.settingsSectionOther))
        theme.setElements(view, root.findViewById(R.id.settingsSectionBottom))

        theme.setElement(isDark, view, 3, root.findViewById(R.id.settingsSectionLanguage))
        theme.setElement(isDark, view, 3, root.findViewById(R.id.settingsSectionListen))
        theme.setElement(isDark, view, 3, root.findViewById(R.id.settingsSectionSpeak))
        theme.setElement(isDark, view, 3, root.findViewById(R.id.settingsSectionOther))
        theme.setElement(isDark, view, 1, root.findViewById(R.id.settingsSectionBottom))


        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.textRelease) as TextView,
            background = false
        )

        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.buttonContactOnTelegram) as TextView,
            background = false
        )
        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.buttonBuyMeACoffee) as TextView,
            background = false
        )
        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.buttonProjectGitHub) as TextView,
            background = false
        )
        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.buttonTranslateTheApp) as TextView,
            background = false
        )
        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.buttonOpenTutorial) as TextView,
            background = false
        )
        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.buttonSeeStatistics) as TextView,
            background = false
        )

        theme.setElement(
            isDark,
            root.findViewById(R.id.imageLanguageIcon) as ImageView,
            R.drawable.ic_language,
            R.drawable.ic_language
        )
    }
}