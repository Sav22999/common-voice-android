package org.commonvoice.saverio.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.MultiAutoCompleteTextView.CommaTokenizer
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_settings.*
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    val languages_list_short = arrayOf("it", "en")
    val languages_list = arrayOf("Italiano", "English")

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
        val model = ViewModelProviders.of(activity!!).get(SettingsViewModel::class.java)
        var selected_language = model.language.observe(this, Observer {
            textSelectedLanguage.text = "Your selected language is: "+it
        })

        return root
    }
}