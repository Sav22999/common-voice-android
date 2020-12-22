package org.commonvoice.saverio.ui.settings.nestedSettings

import android.content.Intent
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_advanced_settings.*
import org.commonvoice.saverio.FirstLaunch
import org.commonvoice.saverio.MessageDialog
import org.commonvoice.saverio.databinding.FragmentAdvancedSettingsBinding
import org.commonvoice.saverio.databinding.ToolbarBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.koin.android.ext.android.inject

class AdvancedSettingsFragment : ViewBoundFragment<FragmentAdvancedSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAdvancedSettingsBinding {
        return FragmentAdvancedSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val mainPrefManager by inject<MainPrefManager>()

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
                mainPrefManager.areAppUsageStats = isChecked
            }
            switchAppUsageStatistics.isChecked = mainPrefManager.areAppUsageStats

            switchSaveLogToFile.setOnCheckedChangeListener { _, isChecked ->
                mainPrefManager.isLogFeature = isChecked
            }
            switchSaveLogToFile.isChecked = mainPrefManager.isLogFeature

            buttonOpenTutorialAgain.setOnClickListener {
                Intent(requireContext(), FirstLaunch::class.java).also {
                    startActivity(it)
                    activity?.finish()
                }
            }

            buttonResetData.setOnClickListener {
                //TODO: reset data (reset all settings to default value and logout)
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