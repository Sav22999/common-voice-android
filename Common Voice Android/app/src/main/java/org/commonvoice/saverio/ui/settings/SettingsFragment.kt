package org.commonvoice.saverio.ui.settings

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import org.commonvoice.saverio.BuildConfig
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    var languages_list_short = arrayOf("en") // don't change it manually -> it will import automatically
    var languages_list = arrayOf("English") // don't change it manually -> it will import automatically

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val textLanguage: TextView = root.findViewById(R.id.text_settingsLanguage)
        textLanguage.text = getText(R.string.settingsLanguage)
        //val model = ViewModelProviders.of(activity!!).get(SettingsViewModel::class.java)

        var main = (activity as MainActivity)

        var releaseNumber: TextView = root.findViewById(R.id.textRelease)
        releaseNumber.text = BuildConfig.VERSION_NAME

        // import the languages list (short and "standard" from mainactivity)
        this.languages_list_short = main.languagesListShortArray
        this.languages_list = main.languagesListArray

        var language: Spinner = root.findViewById(R.id.languageList)
        language.adapter = main.getLanguageList()

        var selected_language: String = main.getSelectedLanguage()

        language.setSelection(languages_list_short.indexOf(selected_language))

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
                main.setLanguageSettings(languages_list_short.get(position))
            }
        }

        var text_project_github: TextView = root.findViewById(R.id.textProjectGitHub)
        text_project_github.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sav22999/common-voice-android"))
            startActivity(browserIntent)
        }
        text_project_github.paintFlags= Paint.UNDERLINE_TEXT_FLAG

        var text_donate_paypal: TextView = root.findViewById(R.id.textDonatePayPal)
        text_donate_paypal.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/saveriomorelli"))
            startActivity(browserIntent)
        }
        text_donate_paypal.paintFlags= Paint.UNDERLINE_TEXT_FLAG

        var btn_open_tutorial: Button = root.findViewById(R.id.buttonOpenTutorial)
        btn_open_tutorial.setOnClickListener {
            main.openTutorial()
        }

        var txt_contributors: TextView = root.findViewById(R.id.textContributors)
        txt_contributors.text = getString(R.string.txt_contributors)

        var txt_developed_by: TextView = root.findViewById(R.id.textDevelopedBy)
        txt_developed_by.text = getString(R.string.txt_developed_by)

        return root
    }
}