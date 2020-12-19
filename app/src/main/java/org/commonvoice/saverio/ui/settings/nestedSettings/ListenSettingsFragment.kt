package org.commonvoice.saverio.ui.settings.nestedSettings

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import org.commonvoice.saverio.MessageDialog
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentListenSettingsBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio_lib.preferences.ListenPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.koin.android.ext.android.inject

class ListenSettingsFragment : ViewBoundFragment<FragmentListenSettingsBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListenSettingsBinding {
        return FragmentListenSettingsBinding.inflate(layoutInflater, container, false)
    }

    private val settingsPrefManager by inject<SettingsPrefManager>()
    private val listenPrefManager by inject<ListenPrefManager>()

    override fun onStart() {
        super.onStart()

        withBinding {
            switchAutoPlayClips.setOnCheckedChangeListener { _, isChecked ->
                listenPrefManager.isAutoPlayClipEnabled = isChecked
            }
            switchAutoPlayClips.isChecked = listenPrefManager.isAutoPlayClipEnabled
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