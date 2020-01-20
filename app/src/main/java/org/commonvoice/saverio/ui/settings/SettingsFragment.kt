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
import androidx.lifecycle.ViewModelProviders
import org.commonvoice.saverio.BuildConfig
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
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
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        var main = (activity as MainActivity)
        main.dashboard_selected = false

        val textLanguage: TextView = root.findViewById(R.id.text_settingsLanguage)
        textLanguage.text = getText(R.string.settingsLanguage)
        //val model = ViewModelProviders.of(activity!!).get(SettingsViewModel::class.java)

        var releaseNumber: TextView = root.findViewById(R.id.textRelease)
        releaseNumber.text = BuildConfig.VERSION_NAME

        if (BuildConfig.VERSION_NAME.contains("a")) {
            this.isAlpha = true
        }

        if (isAlpha) {
            //alpha
            releaseNumber.text = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"
        }

        // import the languages list (short and "standard" from mainactivity)
        this.languagesListShort = main.languagesListShortArray
        this.languagesList = main.languagesListArray

        var language: Spinner = root.findViewById(R.id.languageList)
        language.adapter = main.getLanguageList()

        var selectedLanguage: String = main.getSelectedLanguage()

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

        var textProjectGithub: Button = root.findViewById(R.id.textProjectGitHub)
        textProjectGithub.setOnClickListener {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Sav22999/common-voice-android")
                )
            startActivity(browserIntent)
        }

        var textDonatePaypal: Button = root.findViewById(R.id.textDonatePayPal)
        textDonatePaypal.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/saveriomorelli"))
            startActivity(browserIntent)
        }

        var btnOpenTutorial: Button = root.findViewById(R.id.buttonOpenTutorial)
        btnOpenTutorial.setOnClickListener {
            main.openTutorial()
        }

        var btnWebBrowserForTest: Button = root.findViewById(R.id.buttonOpenWBTests)
        btnWebBrowserForTest.setOnClickListener {
            main.openWebBrowserForTest()
        }

        if (isAlpha) {
            btnWebBrowserForTest.isGone = false
            var separator: View = root.findViewById(R.id.separator4)
            separator.isGone = false
        }

        var txtContributors: TextView = root.findViewById(R.id.textContributors)
        txtContributors.text = getString(R.string.txt_contributors)

        var txtDevelopedBy: TextView = root.findViewById(R.id.textDevelopedBy)
        txtDevelopedBy.text = getString(R.string.txt_developed_by)

        main.checkConnection()

        var switchAutoPlaySettings: Switch = root.findViewById(R.id.switchAutoPlayClips)
        switchAutoPlaySettings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //ON
            } else {
                //OFF
            }
            main.setAutoPlay(isChecked)
        }
        switchAutoPlaySettings.isChecked = main.getAutoPlay()

        var switchDarkThemeSettings: Switch = root.findViewById(R.id.switchDarkTheme)
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

        setTheme(main, root)

        return root
    }

    fun setTheme(view: Context, root: View) {
        theme.setElements(view, root.findViewById(R.id.layoutSettings))

        theme.setElement(
            theme.getTheme(view),
            root.findViewById(R.id.imageLanguageIcon) as ImageView,
            R.drawable.ic_language,
            R.drawable.ic_language_darktheme
        )
    }
}